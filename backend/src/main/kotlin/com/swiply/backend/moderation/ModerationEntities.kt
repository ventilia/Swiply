package com.swiply.backend.moderation

import com.swiply.backend.common.AbstractUuidEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

enum class ReportReason { SPAM, FAKE_PROFILE, INAPPROPRIATE_CONTENT, HARASSMENT, UNDERAGE, OTHER }

enum class ReportStatus { PENDING, REVIEWED, ACTIONED, DISMISSED }

enum class ModerationActionType { WARN, SUSPEND, BAN, UNBAN, PHOTO_REJECT, PHOTO_APPROVE }

@Entity
@Table(name = "reports")
class Report(
    id: UUID = UUID.randomUUID(),

    @Column(name = "reporter_id", nullable = false)
    var reporterId: UUID,

    @Column(name = "target_user_id", nullable = false)
    var targetUserId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var reason: ReportReason,

    @Column(length = 1000)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var status: ReportStatus = ReportStatus.PENDING,

    @Column(name = "reviewed_by")
    var reviewedBy: UUID? = null,

    @Column(name = "reviewed_at")
    var reviewedAt: Instant? = null,
) : AbstractUuidEntity(id) {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
}


@Entity
@Table(name = "moderation_actions")
class ModerationAction(
    id: UUID = UUID.randomUUID(),

    @Column(name = "moderator_id", nullable = false, updatable = false)
    val moderatorId: UUID,

    @Column(name = "target_user_id", nullable = false, updatable = false)
    val targetUserId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, updatable = false)
    val action: ModerationActionType,

    @Column(length = 500, updatable = false)
    val reason: String? = null,
) : AbstractUuidEntity(id) {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
}
