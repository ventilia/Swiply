package com.swiply.backend.profile

import com.swiply.backend.common.Gender
import com.swiply.backend.media.PhotoResponse
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class LocationView(val latitude: Double, val longitude: Double)

data class MyProfileResponse(
    val id: UUID,
    val email: String,
    val emailVerified: Boolean,
    val displayName: String,
    val birthDate: LocalDate,
    val age: Int,
    val gender: Gender,
    val interestedIn: Set<Gender>,
    val interests: List<String>,
    val bio: String?,
    val city: String?,
    val location: LocationView?,
    val minAgePref: Int,
    val maxAgePref: Int,
    val maxDistanceKm: Int,
    val isIncognito: Boolean,
    val isDiscoverable: Boolean,
    val isVerified: Boolean,
    val photos: List<PhotoResponse>,
)

data class UpdateProfileRequest(
    @field:Size(min = 2, max = 40)
    val displayName: String? = null,

    @field:Size(max = 600)
    val bio: String? = null,

    val gender: Gender? = null,

    val interestedIn: Set<Gender>? = null,

    /** Полный новый набор интересов (из каталога); валидируется на сервере */
    val interests: List<String>? = null,

    @field:Size(max = 120)
    val city: String? = null,
)

data class UpdatePreferencesRequest(
    @field:Min(18) @field:Max(100)
    val minAge: Int? = null,

    @field:Min(18) @field:Max(100)
    val maxAge: Int? = null,

    @field:Min(1) @field:Max(300)
    val maxDistanceKm: Int? = null,

    val isIncognito: Boolean? = null,

    val isDiscoverable: Boolean? = null,
)

data class UpdateLocationRequest(
    @field:Min(-90) @field:Max(90)
    val latitude: Double,

    @field:Min(-180) @field:Max(180)
    val longitude: Double,

    @field:Size(max = 120)
    val city: String? = null,
)

data class PublicProfileResponse(
    val id: UUID,
    val displayName: String,
    val age: Int,
    val gender: Gender,
    val interests: List<String>,
    val bio: String?,
    val city: String?,
    val isVerified: Boolean,
    /** Округлённая дистанция в км; точные координаты никогда не отдаются */
    val distanceKm: Int?,
    val isOnline: Boolean,
    val lastSeenAt: Instant?,
    val photos: List<PhotoResponse>,
)
