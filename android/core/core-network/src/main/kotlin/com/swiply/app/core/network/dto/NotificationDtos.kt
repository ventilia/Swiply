package com.swiply.app.core.network.dto

import com.swiply.app.core.model.AppNotification
import com.swiply.app.core.model.NotificationType
import com.swiply.app.core.model.RealtimeEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant
import java.util.UUID

@Serializable
data class NotificationDto(
    val id: String,
    val type: String,
    val payload: JsonObject = JsonObject(emptyMap()),
    val isRead: Boolean = false,
    val createdAt: String,
)

@Serializable
data class UnreadCountDto(val unread: Long = 0)

/** Realtime-событие из /user/queue/events */
@Serializable
data class RealtimeEventDto(
    val type: String,
    val payload: JsonObject = JsonObject(emptyMap()),
)

/** Плоское представление JsonObject: примитивы → строки, null → null */
fun JsonObject.flatten(): Map<String, String?> = entries.associate { (key, value) ->
    key to when (value) {
        is JsonNull -> null
        is JsonPrimitive -> value.content
        else -> value.toString()
    }
}

fun NotificationDto.toDomain() = AppNotification(
    id = UUID.fromString(id),
    type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.SYSTEM),
    payload = payload.flatten(),
    isRead = isRead,
    createdAt = Instant.parse(createdAt),
)

fun RealtimeEventDto.toDomain() = RealtimeEvent(
    type = type,
    payload = payload.flatten(),
)
