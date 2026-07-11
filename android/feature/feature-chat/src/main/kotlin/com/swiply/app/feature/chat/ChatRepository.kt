package com.swiply.app.feature.chat

import android.net.Uri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.swiply.app.core.common.AppError
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.common.map
import com.swiply.app.core.common.media.ImageCompressor
import com.swiply.app.core.common.onSuccess
import com.swiply.app.core.database.dao.ConversationDao
import com.swiply.app.core.database.dao.MessageDao
import com.swiply.app.core.database.entity.MessageEntity
import com.swiply.app.core.database.toDomain
import com.swiply.app.core.database.toEntity
import com.swiply.app.core.model.ChatMessage
import com.swiply.app.core.model.Conversation
import com.swiply.app.core.model.MessageStatus
import com.swiply.app.core.model.MessageType
import com.swiply.app.core.network.ApiCaller
import com.swiply.app.core.network.SessionManager
import com.swiply.app.core.network.api.ChatApi
import com.swiply.app.core.network.dto.toDomain
import com.swiply.app.core.network.realtime.RealtimeClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatApi: ChatApi,
    private val apiCaller: ApiCaller,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val realtimeClient: RealtimeClient,
    private val sessionManager: SessionManager,
    private val imageCompressor: ImageCompressor,
) {

    /** conversationId → nextCursor истории (для APPEND в RemoteMediator) */
    internal val cursors = ConcurrentHashMap<String, String?>()

    val conversations: Flow<List<Conversation>> = conversationDao.observeAll()
        .map { list -> list.map { it.toDomain() } }

    /** Бейдж непрочитанного на таб «Мэтчи» */
    val unreadTotal: Flow<Int> = conversationDao.observeUnreadTotal()

    fun observeConversation(conversationId: String): Flow<Conversation?> =
        conversationDao.observeById(conversationId).map { it?.toDomain() }

    suspend fun refreshConversations(): AppResult<Unit> =
        apiCaller.call { chatApi.conversations() }
            .onSuccess { list ->
                val domains = list.map { it.toDomain() }
                conversationDao.upsertAll(domains.map { it.toEntity() })
                conversationDao.deleteNotIn(domains.map { it.id })
            }
            .map { }

    suspend fun conversationByMatch(matchId: UUID): AppResult<Conversation> =
        apiCaller.call { chatApi.conversationByMatch(matchId.toString()).toDomain() }
            .onSuccess { conversationDao.upsert(it.toEntity()) }

    @OptIn(ExperimentalPagingApi::class)
    fun messagesPager(conversationId: String): Flow<PagingData<ChatMessage>> =
        Pager(
            config = PagingConfig(pageSize = 30, prefetchDistance = 10, enablePlaceholders = false),
            remoteMediator = MessagesRemoteMediator(conversationId, chatApi, messageDao, this),
            pagingSourceFactory = { messageDao.pagingSource(conversationId) },
        ).flow.map { paging -> paging.map { it.toDomain() } }


    suspend fun sendText(conversationId: String, text: String) {
        val me = sessionManager.currentUserId ?: return
        val tempId = "tmp-${UUID.randomUUID()}"
        messageDao.upsert(
            pendingEntity(
                tempId = tempId,
                conversationId = conversationId,
                senderId = me,
                type = MessageType.TEXT,
                content = text.trim(),
                mediaUrl = null,
            ),
        )
        realtimeClient.sendChatMessage(
            conversationId = conversationId,
            content = text.trim(),
            mediaKey = null,
            clientTempId = tempId,
        )
    }


    suspend fun sendImage(conversationId: String, uri: Uri): AppResult<Unit> {
        val me = sessionManager.currentUserId
            ?: return AppResult.Failure(AppError.unknown())
        val bytes = imageCompressor.compress(uri)
            ?: return AppResult.Failure(AppError("BAD_IMAGE", "Не удалось прочитать изображение"))

        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "attachment.jpg",
            body = bytes.toRequestBody("image/jpeg".toMediaType()),
        )
        return apiCaller.call { chatApi.uploadAttachment(conversationId, part) }
            .onSuccess { attachment ->
                val tempId = "tmp-${UUID.randomUUID()}"
                messageDao.upsert(
                    pendingEntity(
                        tempId = tempId,
                        conversationId = conversationId,
                        senderId = me,
                        type = MessageType.IMAGE,
                        content = null,
                        mediaUrl = attachment.url,
                    ),
                )
                realtimeClient.sendChatMessage(
                    conversationId = conversationId,
                    content = null,
                    mediaKey = attachment.mediaKey,
                    clientTempId = tempId,
                )
            }
            .map { }
    }

    suspend fun markRead(conversationId: String) {
        conversationDao.clearUnread(conversationId)

        apiCaller.call { chatApi.markRead(conversationId) }
    }

    fun sendTyping(conversationId: String, isTyping: Boolean) {
        realtimeClient.sendTyping(conversationId, isTyping)
    }

    private fun pendingEntity(
        tempId: String,
        conversationId: String,
        senderId: UUID,
        type: MessageType,
        content: String?,
        mediaUrl: String?,
    ) = MessageEntity(
        id = tempId,
        conversationId = conversationId,
        senderId = senderId.toString(),
        type = type.name,
        content = content,
        mediaUrl = mediaUrl,
        sentAt = Instant.now().toEpochMilli(),
        deliveredAt = null,
        readAt = null,
        status = MessageStatus.SENT.name,
        clientTempId = tempId,
        isPending = true,
    )
}
