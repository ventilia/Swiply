package com.swiply.app.feature.chat

import com.swiply.app.core.common.ApplicationScope
import com.swiply.app.core.database.dao.ConversationDao
import com.swiply.app.core.database.dao.MessageDao
import com.swiply.app.core.database.toEntity
import com.swiply.app.core.model.ChatMessage
import com.swiply.app.core.model.MessageType
import com.swiply.app.core.network.SessionManager
import com.swiply.app.core.network.realtime.RealtimeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class TypingEvent(val conversationId: String, val userId: String, val isTyping: Boolean)


@Singleton
class ChatRealtimeSync @Inject constructor(
    private val realtimeClient: RealtimeClient,
    private val chatRepository: ChatRepository,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val sessionManager: SessionManager,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val _typing = MutableSharedFlow<TypingEvent>(extraBufferCapacity = 32)
    val typing: SharedFlow<TypingEvent> = _typing

    /** conversationId сообщений, пришедших прямо сейчас (для автоскролла/прочтения) */
    private val _incomingMessages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 32)
    val incomingMessages: SharedFlow<ChatMessage> = _incomingMessages

    fun start() {
        scope.launch {
            realtimeClient.messages.collect { message -> onMessage(message) }
        }
        scope.launch {
            realtimeClient.events.collect { event ->
                when (event.type) {
                    "chat.read" -> {
                        val conversationId = event.payload["conversationId"] ?: return@collect
                        val me = sessionManager.currentUserId?.toString() ?: return@collect
                        messageDao.markMyMessagesRead(
                            conversationId = conversationId,
                            myUserId = me,
                            readAtMillis = Instant.now().toEpochMilli(),
                        )
                    }
                    "chat.typing" -> {
                        _typing.tryEmit(
                            TypingEvent(
                                conversationId = event.payload["conversationId"] ?: return@collect,
                                userId = event.payload["userId"] ?: "",
                                isTyping = event.payload["isTyping"]?.toBoolean() ?: false,
                            ),
                        )
                    }
                }
            }
        }
        scope.launch {
            realtimeClient.reconnected.collect {
                chatRepository.refreshConversations()
                resendPending()
            }
        }
    }

    private suspend fun onMessage(message: ChatMessage) {

        message.clientTempId?.let { messageDao.deletePendingByTempId(it) }
        messageDao.upsert(message.toEntity())

        val me = sessionManager.currentUserId
        val incoming = me != null && message.senderId != me


        conversationDao.byId(message.conversationId)?.let { conversation ->
            conversationDao.upsert(
                conversation.copy(
                    lastMessagePreview = when (message.type) {
                        MessageType.TEXT -> message.content?.take(80).orEmpty()
                        MessageType.IMAGE -> "Фото"
                        MessageType.SYSTEM -> message.content.orEmpty()
                    },
                    lastMessageSentAt = message.sentAt.toEpochMilli(),
                    lastMessageSenderId = message.senderId.toString(),
                ),
            )
            if (incoming) {
                conversationDao.incrementUnread(message.conversationId)
            }
        }

        if (incoming) {
            _incomingMessages.tryEmit(message)
        }
    }


    private suspend fun resendPending() {
        messageDao.pendingMessages().forEach { pending ->
            realtimeClient.sendChatMessage(
                conversationId = pending.conversationId,
                content = pending.content,
                mediaKey = null,
                clientTempId = pending.clientTempId ?: pending.id,
            )
        }
    }
}
