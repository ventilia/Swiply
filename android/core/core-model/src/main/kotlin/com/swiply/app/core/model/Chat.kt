package com.swiply.app.core.model

import java.time.Instant
import java.util.UUID

data class ChatPeer(
    val userId: UUID,
    val displayName: String,
    val thumbUrl: String?,
    val isOnline: Boolean,
    val lastSeenAt: Instant?,
)

data class LastMessage(
    val preview: String,
    val sentAt: Instant,
    val senderId: UUID,
)

data class Conversation(
    val id: String,
    val matchId: UUID,
    val peer: ChatPeer,
    val lastMessage: LastMessage?,
    val unreadCount: Int,
    val createdAt: Instant,
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: UUID,
    val type: MessageType,
    val content: String?,
    val mediaUrl: String?,
    val sentAt: Instant,
    val deliveredAt: Instant?,
    val readAt: Instant?,
    val status: MessageStatus,
    /** Локальный id для optimistic-отправки */
    val clientTempId: String? = null,
    /** true, пока сообщение не подтверждено сервером */
    val isPending: Boolean = false,
)
