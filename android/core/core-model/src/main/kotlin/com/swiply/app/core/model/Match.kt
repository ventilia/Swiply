package com.swiply.app.core.model

import java.time.Instant
import java.util.UUID

data class MatchItem(
    val matchId: UUID,
    val userId: UUID,
    val displayName: String,
    val age: Int,
    val thumbUrl: String?,
    val isOnline: Boolean,
    val matchedAt: Instant,
)

data class LikeReceived(
    val userId: UUID,
    val displayName: String,
    val age: Int,
    val thumbUrl: String?,
    val superlike: Boolean,
    val likedAt: Instant,
)

/** Событие «случился мэтч» — для полноэкранного celebration */
data class MatchCelebration(
    val matchId: UUID,
    val otherUserId: UUID,
    val otherDisplayName: String,
    val otherThumbUrl: String?,
)
