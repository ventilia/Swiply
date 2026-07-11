package com.swiply.backend.matching

import com.swiply.backend.common.AbstractUuidEntity
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.io.Serializable
import java.time.Instant
import java.util.UUID

enum class SwipeAction { LIKE, DISLIKE, SUPERLIKE }

@Entity
@Table(name = "swipes")
class Swipe(
    id: UUID = UUID.randomUUID(),

    @Column(name = "from_user_id", nullable = false)
    var fromUserId: UUID,

    @Column(name = "to_user_id", nullable = false)
    var toUserId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var action: SwipeAction,
) : AbstractUuidEntity(id) {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
}


@Entity
@Table(name = "matches")
class Match(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_a_id", nullable = false)
    var userAId: UUID,

    @Column(name = "user_b_id", nullable = false)
    var userBId: UUID,

    @Column(name = "matched_at", nullable = false)
    var matchedAt: Instant = Instant.now(),

    @Column(name = "unmatched_at")
    var unmatchedAt: Instant? = null,

    @Column(name = "unmatched_by")
    var unmatchedBy: UUID? = null,
) : AbstractUuidEntity(id) {

    fun otherUserId(userId: UUID): UUID = if (userAId == userId) userBId else userAId

    fun involves(userId: UUID): Boolean = userAId == userId || userBId == userId

    val active: Boolean
        get() = unmatchedAt == null
}

@Embeddable
data class BlockedUserId(
    @Column(name = "blocker_id")
    val blockerId: UUID = UUID(0, 0),

    @Column(name = "blocked_id")
    val blockedId: UUID = UUID(0, 0),
) : Serializable

@Entity
@Table(name = "blocked_users")
class BlockedUser(
    @EmbeddedId
    val id: BlockedUserId,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
}
