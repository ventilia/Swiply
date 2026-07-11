package com.swiply.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val peerUserId: String,
    val peerDisplayName: String,
    val peerThumbUrl: String?,
    val peerIsOnline: Boolean,
    val peerLastSeenAt: Long?,
    val lastMessagePreview: String?,
    val lastMessageSentAt: Long?,
    val lastMessageSenderId: String?,
    val unreadCount: Int,
    val createdAt: Long,
)

@Entity(
    tableName = "messages",
    indices = [Index(value = ["conversationId", "sentAt"])],
)
data class MessageEntity(
    /** id сервера; для неотправленных — clientTempId */
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String,
    val content: String?,
    val mediaUrl: String?,
    val sentAt: Long,
    val deliveredAt: Long?,
    val readAt: Long?,
    val status: String,
    val clientTempId: String?,
    val isPending: Boolean,
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val matchId: String,
    val userId: String,
    val displayName: String,
    val age: Int,
    val thumbUrl: String?,
    val isOnline: Boolean,
    val matchedAt: Long,
)

/** Кэш собственного профиля: одна строка с JSON-снапшотом (id всегда 1) */
@Entity(tableName = "my_profile")
data class MyProfileEntity(
    @PrimaryKey val id: Int = 1,
    val json: String,
)
