package com.swiply.backend.chat

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

enum class MessageType { TEXT, IMAGE, SYSTEM }

enum class MessageStatus { SENT, DELIVERED, READ }


@Document("conversations")
class Conversation(
    @Id
    var id: String? = null,

    @Indexed(unique = true)
    var matchId: UUID,

    var participantIds: List<UUID>,

    var lastMessageAt: Instant? = null,

    var lastMessagePreview: String? = null,

    var lastMessageSenderId: UUID? = null,


    var unreadCount: MutableMap<String, Int> = mutableMapOf(),

    var createdAt: Instant = Instant.now(),
) {
    fun otherParticipant(userId: UUID): UUID =
        participantIds.first { it != userId }

    fun involves(userId: UUID): Boolean = participantIds.contains(userId)
}

@Document("messages")
class ChatMessage(
    @Id
    var id: String? = null,

    @Indexed
    var conversationId: String,

    var senderId: UUID,

    var type: MessageType = MessageType.TEXT,

    var content: String? = null,


    var mediaKey: String? = null,

    @Indexed
    var sentAt: Instant = Instant.now(),

    var deliveredAt: Instant? = null,

    var readAt: Instant? = null,

    var status: MessageStatus = MessageStatus.SENT,
)
