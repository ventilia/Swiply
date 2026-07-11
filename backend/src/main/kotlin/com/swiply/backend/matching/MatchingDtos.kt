package com.swiply.backend.matching

import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class SwipeRequest(
    @field:NotNull
    val toUserId: UUID,

    @field:NotNull
    val action: SwipeAction,
)

data class SwipeResponse(
    val matched: Boolean,
    val matchId: UUID? = null,
    val remainingLikes: Long? = null,
)

data class UndoSwipeResponse(
    val toUserId: UUID,
    val action: SwipeAction,
)

data class MatchItemResponse(
    val matchId: UUID,
    val userId: UUID,
    val displayName: String,
    val age: Int,
    val thumbUrl: String?,
    val isOnline: Boolean,
    val matchedAt: Instant,
)

data class LikeReceivedItem(
    val userId: UUID,
    val displayName: String,
    val age: Int,
    val thumbUrl: String?,
    val superlike: Boolean,
    val likedAt: Instant,
)

data class BlockedUserItem(
    val userId: UUID,
    val displayName: String?,
    val blockedAt: Instant,
)
