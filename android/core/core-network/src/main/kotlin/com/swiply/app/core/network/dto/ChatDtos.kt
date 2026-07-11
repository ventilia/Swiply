package com.swiply.app.core.network.dto

import com.swiply.app.core.model.ChatMessage
import com.swiply.app.core.model.ChatPeer
import com.swiply.app.core.model.Conversation
import com.swiply.app.core.model.LastMessage
import com.swiply.app.core.model.MessageStatus
import com.swiply.app.core.model.MessageType
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class ChatPeerDto(
    val userId: String,
    val displayName: String,
    val thumbUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
)

@Serializable
data class LastMessageDto(
    val preview: String,
    val sentAt: String,
    val senderId: String,
)

@Serializable
data class ConversationDto(
    val id: String,
    val matchId: String,
    val peer: ChatPeerDto,
    val lastMessage: LastMessageDto? = null,
    val unreadCount: Int = 0,
    val createdAt: String,
)

fun ConversationDto.toDomain() = Conversation(
    id = id,
    matchId = UUID.fromString(matchId),
    peer = ChatPeer(
        userId = UUID.fromString(peer.userId),
        displayName = peer.displayName,
        thumbUrl = peer.thumbUrl,
        isOnline = peer.isOnline,
        lastSeenAt = peer.lastSeenAt?.let { Instant.parse(it) },
    ),
    lastMessage = lastMessage?.let {
        LastMessage(
            preview = it.preview,
            sentAt = Instant.parse(it.sentAt),
            senderId = UUID.fromString(it.senderId),
        )
    },
    unreadCount = unreadCount,
    createdAt = Instant.parse(createdAt),
)

@Serializable
data class MessageDto(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String = "TEXT",
    val content: String? = null,
    val mediaUrl: String? = null,
    val sentAt: String,
    val deliveredAt: String? = null,
    val readAt: String? = null,
    val status: String = "SENT",
    val clientTempId: String? = null,
)

fun MessageDto.toDomain() = ChatMessage(
    id = id,
    conversationId = conversationId,
    senderId = UUID.fromString(senderId),
    type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.TEXT),
    content = content,
    mediaUrl = mediaUrl,
    sentAt = Instant.parse(sentAt),
    deliveredAt = deliveredAt?.let { Instant.parse(it) },
    readAt = readAt?.let { Instant.parse(it) },
    status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
    clientTempId = clientTempId,
)

@Serializable
data class AttachmentDto(
    val mediaKey: String,
    val url: String,
)

/** Исходящий STOMP-payload /app/chat.send */
@Serializable
data class ChatSendPayloadDto(
    val conversationId: String,
    val type: String = "TEXT",
    val content: String? = null,
    val mediaKey: String? = null,
    val clientTempId: String? = null,
)

@Serializable
data class TypingPayloadDto(
    val conversationId: String,
    val isTyping: Boolean,
)

@Serializable
data class ReadPayloadDto(
    val conversationId: String,
)
