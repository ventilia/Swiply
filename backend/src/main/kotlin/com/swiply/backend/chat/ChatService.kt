package com.swiply.backend.chat

import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.Cursor
import com.swiply.backend.common.ForbiddenException
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.common.PageResponse
import com.swiply.backend.common.RateLimiter
import com.swiply.backend.config.SwiplyProperties
import com.swiply.backend.matching.BlockedUserRepository
import com.swiply.backend.matching.MatchRepository
import com.swiply.backend.media.ImageProcessor
import com.swiply.backend.media.MediaStorage
import com.swiply.backend.media.PhotoService
import com.swiply.backend.notification.NotificationService
import com.swiply.backend.notification.NotificationType
import com.swiply.backend.notification.RealtimeEvent
import com.swiply.backend.notification.RealtimeNotifier
import com.swiply.backend.profile.ProfileRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class ChatService(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: ChatMessageRepository,
    private val mongoTemplate: MongoTemplate,
    private val matchRepository: MatchRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val profileRepository: ProfileRepository,
    private val photoService: PhotoService,
    private val presenceService: PresenceService,
    private val notificationService: NotificationService,
    private val realtimeNotifier: RealtimeNotifier,
    private val messagingTemplate: SimpMessagingTemplate,
    private val storage: MediaStorage,
    private val imageProcessor: ImageProcessor,
    private val rateLimiter: RateLimiter,
    private val props: SwiplyProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_MESSAGE_LENGTH = 2000
        private const val PREVIEW_LENGTH = 80
    }

    /** Возвращает (или лениво создаёт) диалог по мэтчу. */
    fun conversationByMatch(userId: UUID, matchId: UUID): ConversationResponse {
        val match = matchRepository.findById(matchId).orElse(null)
        if (match == null || !match.involves(userId)) {
            throw NotFoundException("MATCH_NOT_FOUND", "Мэтч не найден")
        }
        if (!match.active) {
            throw ForbiddenException("MATCH_INACTIVE", "Мэтч разорван")
        }
        val conversation = conversationRepository.findByMatchId(matchId) ?: conversationRepository.save(
            Conversation(
                matchId = matchId,
                participantIds = listOf(match.userAId, match.userBId),
                unreadCount = mutableMapOf(
                    match.userAId.toString() to 0,
                    match.userBId.toString() to 0,
                ),
            ),
        )
        return toConversationResponse(conversation, userId)
    }

    fun conversations(userId: UUID): List<ConversationResponse> =
        conversationRepository.findByParticipantIdsOrderByLastMessageAtDesc(userId)
            .map { toConversationResponse(it, userId) }

    fun messages(userId: UUID, conversationId: String, cursor: String?, limit: Int): PageResponse<MessageResponse> {
        val conversation = requireParticipant(userId, conversationId)

        val criteria = Criteria.where("conversationId").`is`(conversationId)
        val decoded = Cursor.decode(cursor)
        if (decoded != null) {
            val parts = decoded.split(":")
            val ts = parts.getOrNull(0)?.toLongOrNull()?.let { Instant.ofEpochMilli(it) }
                ?: throw BadRequestException("BAD_CURSOR", "Некорректный курсор")
            val id = parts.getOrNull(1)
            if (id != null && ObjectId.isValid(id)) {
                criteria.orOperator(
                    Criteria.where("sentAt").lt(ts),
                    Criteria.where("sentAt").`is`(ts).and("_id").lt(ObjectId(id)),
                )
            } else {
                criteria.and("sentAt").lt(ts)
            }
        }

        val query = Query(criteria)
            .with(Sort.by(Sort.Direction.DESC, "sentAt", "_id"))
            .limit(limit + 1)
        val batch = mongoTemplate.find(query, ChatMessage::class.java)
        val page = batch.take(limit)

        markDelivered(conversation, userId, page)

        val items = page.map { toMessageResponse(it) }
        val next = if (batch.size > limit) {
            val last = page.last()
            Cursor.encode("${last.sentAt.toEpochMilli()}:${last.id}")
        } else {
            null
        }
        return PageResponse(items, next)
    }

    fun sendMessage(senderId: UUID, payload: ChatSendPayload): MessageResponse {
        val conversation = requireParticipant(senderId, payload.conversationId)
        val receiverId = conversation.otherParticipant(senderId)

        ensureChatAllowed(senderId, receiverId, conversation)
        rateLimiter.acquire("message", senderId.toString(), props.limits.messagesPer10s, Duration.ofSeconds(10))

        val message = when (payload.type) {
            MessageType.TEXT -> {
                val content = payload.content?.trim()
                if (content.isNullOrEmpty()) throw BadRequestException("EMPTY_MESSAGE", "Пустое сообщение")
                if (content.length > MAX_MESSAGE_LENGTH) {
                    throw BadRequestException("MESSAGE_TOO_LONG", "Сообщение длиннее $MAX_MESSAGE_LENGTH символов")
                }
                ChatMessage(
                    conversationId = payload.conversationId,
                    senderId = senderId,
                    type = MessageType.TEXT,
                    content = content,
                )
            }
            MessageType.IMAGE -> {
                val key = payload.mediaKey
                    ?: throw BadRequestException("MEDIA_KEY_REQUIRED", "Для фото-сообщения нужен mediaKey")

                if (!key.startsWith("chats/${payload.conversationId}/")) {
                    throw BadRequestException("BAD_MEDIA_KEY", "Некорректный mediaKey")
                }
                ChatMessage(
                    conversationId = payload.conversationId,
                    senderId = senderId,
                    type = MessageType.IMAGE,
                    mediaKey = key,
                )
            }
            MessageType.SYSTEM -> throw BadRequestException("BAD_TYPE", "SYSTEM-сообщения создаёт только сервер")
        }

        val receiverOnline = presenceService.isOnline(receiverId)
        if (receiverOnline) {
            message.deliveredAt = Instant.now()
            message.status = MessageStatus.DELIVERED
        }
        val saved = messageRepository.save(message)


        conversation.lastMessageAt = saved.sentAt
        conversation.lastMessagePreview = previewOf(saved)
        conversation.lastMessageSenderId = senderId
        conversation.unreadCount[receiverId.toString()] =
            (conversation.unreadCount[receiverId.toString()] ?: 0) + 1
        conversationRepository.save(conversation)

        val response = toMessageResponse(saved, payload.clientTempId)

        messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/messages", response)
        messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", response)

        if (!receiverOnline) {
            val senderName = profileRepository.findById(senderId).orElse(null)?.displayName ?: "Новое сообщение"
            notificationService.notify(
                userId = receiverId,
                type = NotificationType.NEW_MESSAGE,
                payload = mapOf(
                    "conversationId" to saved.conversationId,
                    "senderId" to senderId.toString(),
                    "preview" to previewOf(saved),
                ),
                pushTitle = senderName,
                pushBody = previewOf(saved),
            )
        }
        return response
    }


    fun markRead(userId: UUID, conversationId: String) {
        val conversation = requireParticipant(userId, conversationId)
        val otherId = conversation.otherParticipant(userId)
        val now = Instant.now()

        mongoTemplate.updateMulti(
            Query(
                Criteria.where("conversationId").`is`(conversationId)
                    .and("senderId").`is`(otherId)
                    .and("readAt").`is`(null),
            ),
            Update().set("readAt", now).set("status", MessageStatus.READ.name)
                .set("deliveredAt", now),
            ChatMessage::class.java,
        )
        conversation.unreadCount[userId.toString()] = 0
        conversationRepository.save(conversation)

        realtimeNotifier.sendEvent(
            otherId,
            RealtimeEvent(
                "chat.read",
                mapOf("conversationId" to conversationId, "readerId" to userId.toString(), "readAt" to now.toString()),
            ),
        )
    }


    fun typing(userId: UUID, payload: TypingPayload) {
        val conversation = requireParticipant(userId, payload.conversationId)
        realtimeNotifier.sendEvent(
            conversation.otherParticipant(userId),
            RealtimeEvent(
                "chat.typing",
                mapOf(
                    "conversationId" to payload.conversationId,
                    "userId" to userId.toString(),
                    "isTyping" to payload.isTyping,
                ),
            ),
        )
    }


    fun uploadAttachment(userId: UUID, conversationId: String, file: MultipartFile): AttachmentResponse {
        requireParticipant(userId, conversationId)
        if (file.size > props.media.maxPhotoBytes) {
            throw BadRequestException("FILE_TOO_LARGE", "Файл слишком большой")
        }
        val processed = imageProcessor.reencodeToJpeg(file.bytes, props.media.maxDimensionPx)
        val key = "chats/$conversationId/${UUID.randomUUID()}.jpg"
        storage.put(key, processed.bytes, "image/jpeg")
        return AttachmentResponse(mediaKey = key, url = storage.presignedGetUrl(key))
    }



    private fun requireParticipant(userId: UUID, conversationId: String): Conversation {
        val conversation = conversationRepository.findById(conversationId).orElse(null)
            ?: throw NotFoundException("CONVERSATION_NOT_FOUND", "Диалог не найден")
        if (!conversation.involves(userId)) {
            throw NotFoundException("CONVERSATION_NOT_FOUND", "Диалог не найден")
        }
        return conversation
    }

    private fun ensureChatAllowed(senderId: UUID, receiverId: UUID, conversation: Conversation) {
        if (blockedUserRepository.existsEitherWay(senderId, receiverId)) {
            throw ForbiddenException("CHAT_BLOCKED", "Переписка недоступна")
        }
        val match = matchRepository.findById(conversation.matchId).orElse(null)
        if (match == null || !match.active) {
            throw ForbiddenException("MATCH_INACTIVE", "Мэтч разорван — переписка закрыта")
        }
    }


    private fun markDelivered(conversation: Conversation, readerId: UUID, page: List<ChatMessage>) {
        val undelivered = page.filter { it.senderId != readerId && it.deliveredAt == null }
        if (undelivered.isEmpty()) return
        mongoTemplate.updateMulti(
            Query(Criteria.where("_id").`in`(undelivered.map { ObjectId(it.id) })),
            Update().set("deliveredAt", Instant.now()).set("status", MessageStatus.DELIVERED.name),
            ChatMessage::class.java,
        )
    }

    private fun previewOf(message: ChatMessage): String = when (message.type) {
        MessageType.TEXT -> (message.content ?: "").take(PREVIEW_LENGTH)
        MessageType.IMAGE -> "📷 Фото"
        MessageType.SYSTEM -> message.content ?: ""
    }

    private fun toMessageResponse(message: ChatMessage, clientTempId: String? = null) = MessageResponse(
        id = message.id ?: "",
        conversationId = message.conversationId,
        senderId = message.senderId,
        type = message.type,
        content = message.content,
        mediaUrl = message.mediaKey?.let { storage.presignedGetUrl(it) },
        sentAt = message.sentAt,
        deliveredAt = message.deliveredAt,
        readAt = message.readAt,
        status = message.status,
        clientTempId = clientTempId,
    )

    private fun toConversationResponse(conversation: Conversation, viewerId: UUID): ConversationResponse {
        val peerId = conversation.otherParticipant(viewerId)
        val profile = profileRepository.findById(peerId).orElse(null)
        val presence = presenceService.presenceOf(peerId)
        return ConversationResponse(
            id = conversation.id ?: "",
            matchId = conversation.matchId,
            peer = ChatPeerView(
                userId = peerId,
                displayName = profile?.displayName ?: "Пользователь",
                thumbUrl = photoService.listApprovedFor(peerId).firstOrNull()?.thumbUrl,
                isOnline = presence.online,
                lastSeenAt = presence.lastSeenAt,
            ),
            lastMessage = conversation.lastMessageAt?.let {
                LastMessageView(
                    preview = conversation.lastMessagePreview ?: "",
                    sentAt = it,
                    senderId = conversation.lastMessageSenderId ?: peerId,
                )
            },
            unreadCount = conversation.unreadCount[viewerId.toString()] ?: 0,
            createdAt = conversation.createdAt,
        )
    }
}
