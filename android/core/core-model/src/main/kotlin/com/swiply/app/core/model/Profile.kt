package com.swiply.app.core.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class Photo(
    val id: UUID,
    val position: Int,
    val status: PhotoStatus,
    val url: String,
    val thumbUrl: String?,
)

data class MyProfile(
    val id: UUID,
    val email: String,
    val emailVerified: Boolean,
    val displayName: String,
    val birthDate: LocalDate,
    val age: Int,
    val gender: Gender,
    val interestedIn: Set<Gender>,
    val interests: List<String> = emptyList(),
    val bio: String?,
    val city: String?,
    val minAgePref: Int,
    val maxAgePref: Int,
    val maxDistanceKm: Int,
    val isIncognito: Boolean,
    val isDiscoverable: Boolean,
    val isVerified: Boolean,
    val photos: List<Photo>,
) {
    /** Полнота профиля 0..1 — для прогресса в редакторе */
    val completeness: Float
        get() {
            var score = 0.4f
            if (!bio.isNullOrBlank()) score += 0.2f
            if (photos.isNotEmpty()) score += 0.2f
            if (photos.size >= 3) score += 0.1f
            if (city != null) score += 0.1f
            return score.coerceAtMost(1f)
        }
}

data class PublicProfile(
    val id: UUID,
    val displayName: String,
    val age: Int,
    val gender: Gender,
    val interests: List<String> = emptyList(),
    val bio: String?,
    val city: String?,
    val isVerified: Boolean,
    val distanceKm: Int?,
    val isOnline: Boolean,
    val lastSeenAt: Instant?,
    val photos: List<Photo>,
)
