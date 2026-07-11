package com.swiply.app.core.network.dto

import com.swiply.app.core.model.Candidate
import com.swiply.app.core.model.Gender
import com.swiply.app.core.model.LikeReceived
import com.swiply.app.core.model.MatchItem
import com.swiply.app.core.model.SwipeResult
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class CandidateDto(
    val userId: String,
    val displayName: String,
    val age: Int,
    val gender: String,
    val interests: List<String> = emptyList(),
    val bio: String? = null,
    val city: String? = null,
    val distanceKm: Int = 1,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val photos: List<PhotoDto> = emptyList(),
)

fun CandidateDto.toDomain() = Candidate(
    userId = UUID.fromString(userId),
    displayName = displayName,
    age = age,
    gender = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.OTHER),
    interests = interests,
    bio = bio,
    city = city,
    distanceKm = distanceKm,
    isVerified = isVerified,
    isOnline = isOnline,
    photos = photos.map { it.toDomain() },
)

@Serializable
data class SwipeRequestDto(
    val toUserId: String,
    val action: String,
)

@Serializable
data class SwipeResponseDto(
    val matched: Boolean,
    val matchId: String? = null,
    val remainingLikes: Long? = null,
)

fun SwipeResponseDto.toDomain() = SwipeResult(
    matched = matched,
    matchId = matchId?.let { UUID.fromString(it) },
    remainingLikes = remainingLikes,
)

@Serializable
data class UndoSwipeResponseDto(
    val toUserId: String,
    val action: String,
)

@Serializable
data class MatchItemDto(
    val matchId: String,
    val userId: String,
    val displayName: String,
    val age: Int,
    val thumbUrl: String? = null,
    val isOnline: Boolean = false,
    val matchedAt: String,
)

fun MatchItemDto.toDomain() = MatchItem(
    matchId = UUID.fromString(matchId),
    userId = UUID.fromString(userId),
    displayName = displayName,
    age = age,
    thumbUrl = thumbUrl,
    isOnline = isOnline,
    matchedAt = Instant.parse(matchedAt),
)

@Serializable
data class LikeReceivedDto(
    val userId: String,
    val displayName: String,
    val age: Int,
    val thumbUrl: String? = null,
    val superlike: Boolean = false,
    val likedAt: String,
)

fun LikeReceivedDto.toDomain() = LikeReceived(
    userId = UUID.fromString(userId),
    displayName = displayName,
    age = age,
    thumbUrl = thumbUrl,
    superlike = superlike,
    likedAt = Instant.parse(likedAt),
)

@Serializable
data class ReportRequestDto(
    val targetUserId: String,
    val reason: String,
    val description: String? = null,
)
