package com.swiply.app.core.database

import com.swiply.app.core.database.entity.ConversationEntity
import com.swiply.app.core.database.entity.MatchEntity
import com.swiply.app.core.database.entity.MessageEntity
import com.swiply.app.core.model.ChatMessage
import com.swiply.app.core.model.ChatPeer
import com.swiply.app.core.model.Conversation
import com.swiply.app.core.model.LastMessage
import com.swiply.app.core.model.MatchItem
import com.swiply.app.core.model.MessageStatus
import com.swiply.app.core.model.MessageType
import java.time.Instant
import java.util.UUID

// Мапперы entity → domain живут рядом с базой: их используют и chat, и match.

fun ConversationEntity.toDomain() = Conversation(
    id = id,
    matchId = UUID.fromString(matchId),
    peer = ChatPeer(
        userId = UUID.fromString(peerUserId),
        displayName = peerDisplayName,
        thumbUrl = peerThumbUrl,
        isOnline = peerIsOnline,
        lastSeenAt = peerLastSeenAt?.let { Instant.ofEpochMilli(it) },
    ),
    lastMessage = lastMessageSentAt?.let {
        LastMessage(
            preview = lastMessagePreview.orEmpty(),
            sentAt = Instant.ofEpochMilli(it),
            senderId = UUID.fromString(lastMessageSenderId ?: peerUserId),
        )
    },
    unreadCount = unreadCount,
    createdAt = Instant.ofEpochMilli(createdAt),
)

fun Conversation.toEntity() = ConversationEntity(
    id = id,
    matchId = matchId.toString(),
    peerUserId = peer.userId.toString(),
    peerDisplayName = peer.displayName,
    peerThumbUrl = peer.thumbUrl,
    peerIsOnline = peer.isOnline,
    peerLastSeenAt = peer.lastSeenAt?.toEpochMilli(),
    lastMessagePreview = lastMessage?.preview,
    lastMessageSentAt = lastMessage?.sentAt?.toEpochMilli(),
    lastMessageSenderId = lastMessage?.senderId?.toString(),
    unreadCount = unreadCount,
    createdAt = createdAt.toEpochMilli(),
)

fun MessageEntity.toDomain() = ChatMessage(
    id = id,
    conversationId = conversationId,
    senderId = UUID.fromString(senderId),
    type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.TEXT),
    content = content,
    mediaUrl = mediaUrl,
    sentAt = Instant.ofEpochMilli(sentAt),
    deliveredAt = deliveredAt?.let { Instant.ofEpochMilli(it) },
    readAt = readAt?.let { Instant.ofEpochMilli(it) },
    status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
    clientTempId = clientTempId,
    isPending = isPending,
)

fun ChatMessage.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    senderId = senderId.toString(),
    type = type.name,
    content = content,
    mediaUrl = mediaUrl,
    sentAt = sentAt.toEpochMilli(),
    deliveredAt = deliveredAt?.toEpochMilli(),
    readAt = readAt?.toEpochMilli(),
    status = status.name,
    clientTempId = clientTempId,
    isPending = isPending,
)

fun MatchEntity.toDomain() = MatchItem(
    matchId = UUID.fromString(matchId),
    userId = UUID.fromString(userId),
    displayName = displayName,
    age = age,
    thumbUrl = thumbUrl,
    isOnline = isOnline,
    matchedAt = Instant.ofEpochMilli(matchedAt),
)

fun MatchItem.toEntity() = MatchEntity(
    matchId = matchId.toString(),
    userId = userId.toString(),
    displayName = displayName,
    age = age,
    thumbUrl = thumbUrl,
    isOnline = isOnline,
    matchedAt = matchedAt.toEpochMilli(),
)
