package com.swiply.app.core.model

import java.util.UUID

data class Candidate(
    val userId: UUID,
    val displayName: String,
    val age: Int,
    val gender: Gender,
    val interests: List<String> = emptyList(),
    val bio: String?,
    val city: String?,
    val distanceKm: Int,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val photos: List<Photo>,
)

data class SwipeResult(
    val matched: Boolean,
    val matchId: UUID?,
    val remainingLikes: Long?,
)

data class Page<T>(
    val items: List<T>,
    val nextCursor: String?,
)
