package com.swiply.backend.chat

import java.time.Instant
import java.util.UUID

data class ChatPeerView(
    val userId: UUID,
    val displayName: String,
    val thumbUrl: String?,
    val isOnline: Boolean,
    val lastSeenAt: Instant?,
)

data class LastMessageView(
    val preview: String,
    val sentAt: Instant,
    val senderId: UUID,
)

data class ConversationResponse(
    val id: String,
    val matchId: UUID,
    val peer: ChatPeerView,
    val lastMessage: LastMessageView?,
    val unreadCount: Int,
    val createdAt: Instant,
)

data class MessageResponse(
    val id: String,
    val conversationId: String,
    val senderId: UUID,
    val type: MessageType,
    val content: String?,
    val mediaUrl: String?,
    val sentAt: Instant,
    val deliveredAt: Instant?,
    val readAt: Instant?,
    val status: MessageStatus
    val clientTempId: String? = null,
)

/** STOMP SEND /app/chat.send */
data class ChatSendPayload(
    val conversationId: String,
    val type: MessageType = MessageType.TEXT,
    val content: String? = null,
    val mediaKey: String? = null,
    val clientTempId: String? = null,
)

/** STOMP SEND /app/chat.typing */
data class TypingPayload(
    val conversationId: String,
    val isTyping: Boolean,
)

/** STOMP SEND /app/chat.read */
data class ReadPayload(
    val conversationId: String,
)

data class AttachmentResponse(
    val mediaKey: String,
    val url: String,
)
