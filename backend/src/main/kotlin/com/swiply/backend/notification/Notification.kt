package com.swiply.backend.notification

import com.swiply.backend.common.AbstractUuidEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

enum class NotificationType { NEW_MATCH, NEW_MESSAGE, NEW_LIKE, MODERATION, SYSTEM }

@Entity
@Table(name = "notifications")
class Notification(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var type: NotificationType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var payload: MutableMap<String, Any?> = mutableMapOf(),

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,
) : AbstractUuidEntity(id) {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
}
