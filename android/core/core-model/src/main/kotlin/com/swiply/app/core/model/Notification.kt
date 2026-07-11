package com.swiply.app.core.model

import java.time.Instant
import java.util.UUID

data class AppNotification(
    val id: UUID,
    val type: NotificationType,
    /** Плоский payload: значения приведены к строкам */
    val payload: Map<String, String?>,
    val isRead: Boolean,
    val createdAt: Instant,
) {
    val matchId: String? get() = payload["matchId"]
    val conversationId: String? get() = payload["conversationId"]
    val displayName: String? get() = payload["displayName"]
    val thumbUrl: String? get() = payload["thumbUrl"]
    val preview: String? get() = payload["preview"]
}

/** Realtime-событие из /user/queue/events */
data class RealtimeEvent(
    val type: String,
    val payload: Map<String, String?>,
)
