package com.swiply.backend.notification

import com.swiply.backend.common.AfterCommitPublisher
import com.swiply.backend.common.Cursor
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.common.PageResponse
import com.swiply.backend.config.RabbitConfig
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val type: NotificationType,
    val payload: Map<String, Any?>,
    val isRead: Boolean,
    val createdAt: Instant,
)

data class UnreadCountResponse(val unread: Long)

data class PushSendTask(
    val userId: UUID,
    val type: NotificationType,
    val title: String,
    val body: String,
)

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val realtimeNotifier: RealtimeNotifier,
    private val publisher: AfterCommitPublisher,
) {


    @Transactional
    fun notify(
        userId: UUID,
        type: NotificationType,
        payload: Map<String, Any?>,
        pushTitle: String,
        pushBody: String,
    ): Notification {
        val notification = notificationRepository.save(
            Notification(userId = userId, type = type, payload = payload.toMutableMap()),
        )
        realtimeNotifier.sendEvent(
            userId,
            RealtimeEvent(
                type = "notification.created",
                payload = mapOf(
                    "id" to notification.id.toString(),
                    "notificationType" to type.name,
                    "payload" to payload,
                ),
            ),
        )
        publisher.publish(
            RabbitConfig.EXCHANGE,
            RabbitConfig.QUEUE_PUSH_SEND,
            PushSendTask(userId, type, pushTitle, pushBody),
        )
        return notification
    }

    @Transactional(readOnly = true)
    fun list(userId: UUID, unreadOnly: Boolean, cursor: String?, limit: Int): PageResponse<NotificationResponse> {
        val decoded = Cursor.decode(cursor)
        val (ts, id) = parseCursor(decoded)
        val items = notificationRepository.findPage(userId, unreadOnly, ts, id, PageRequest.of(0, limit + 1))
        val page = items.take(limit).map {
            NotificationResponse(it.id, it.type, it.payload, it.isRead, it.createdAt)
        }
        val next = if (items.size > limit) {
            page.last().let { Cursor.encode("${it.createdAt.toEpochMilli()}:${it.id}") }
        } else {
            null
        }
        return PageResponse(page, next)
    }

    @Transactional
    fun markRead(userId: UUID, notificationId: UUID) {
        val updated = notificationRepository.markRead(notificationId, userId)
        if (updated == 0) throw NotFoundException("NOTIFICATION_NOT_FOUND", "Уведомление не найдено")
    }

    @Transactional
    fun markAllRead(userId: UUID) {
        notificationRepository.markAllRead(userId)
    }

    @Transactional(readOnly = true)
    fun unreadCount(userId: UUID): UnreadCountResponse =
        UnreadCountResponse(notificationRepository.countByUserIdAndIsReadFalse(userId))

    private fun parseCursor(decoded: String?): Pair<Instant?, UUID?> {
        if (decoded == null) return null to null
        val parts = decoded.split(":")
        val ts = parts.getOrNull(0)?.toLongOrNull()?.let { Instant.ofEpochMilli(it) }
        val id = parts.getOrNull(1)?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        return ts to id
    }
}
