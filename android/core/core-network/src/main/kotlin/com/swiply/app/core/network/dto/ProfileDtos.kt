package com.swiply.app.core.network.dto

import com.swiply.app.core.model.Gender
import com.swiply.app.core.model.MyProfile
import com.swiply.app.core.model.Photo
import com.swiply.app.core.model.PhotoStatus
import com.swiply.app.core.model.PublicProfile
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Serializable
data class PhotoDto(
    val id: String,
    val position: Int,
    val status: String,
    val url: String,
    val thumbUrl: String? = null,
)

fun PhotoDto.toDomain() = Photo(
    id = UUID.fromString(id),
    position = position,
    status = runCatching { PhotoStatus.valueOf(status) }.getOrDefault(PhotoStatus.PENDING),
    url = url,
    thumbUrl = thumbUrl,
)

@Serializable
data class MyProfileDto(
    val id: String,
    val email: String,
    val emailVerified: Boolean = false,
    val displayName: String,
    val birthDate: String,
    val age: Int,
    val gender: String,
    val interestedIn: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val bio: String? = null,
    val city: String? = null,
    val minAgePref: Int = 18,
    val maxAgePref: Int = 100,
    val maxDistanceKm: Int = 50,
    val isIncognito: Boolean = false,
    val isDiscoverable: Boolean = true,
    val isVerified: Boolean = false,
    val photos: List<PhotoDto> = emptyList(),
)

fun MyProfileDto.toDomain() = MyProfile(
    id = UUID.fromString(id),
    email = email,
    emailVerified = emailVerified,
    displayName = displayName,
    birthDate = LocalDate.parse(birthDate),
    age = age,
    gender = Gender.valueOf(gender),
    interestedIn = interestedIn.map { Gender.valueOf(it) }.toSet(),
    interests = interests,
    bio = bio,
    city = city,
    minAgePref = minAgePref,
    maxAgePref = maxAgePref,
    maxDistanceKm = maxDistanceKm,
    isIncognito = isIncognito,
    isDiscoverable = isDiscoverable,
    isVerified = isVerified,
    photos = photos.map { it.toDomain() },
)

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val bio: String? = null,
    val gender: String? = null,
    val interestedIn: List<String>? = null,
    val interests: List<String>? = null,
    val city: String? = null,
)

@Serializable
data class UpdatePreferencesRequestDto(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val maxDistanceKm: Int? = null,
    val isIncognito: Boolean? = null,
    val isDiscoverable: Boolean? = null,
)

@Serializable
data class UpdateLocationRequestDto(
    val latitude: Double,
    val longitude: Double,
    val city: String? = null,
)

@Serializable
data class DeleteAccountRequestDto(val password: String)

@Serializable
data class BlockedUserItemDto(
    val userId: String,
    val displayName: String? = null,
    val blockedAt: String,
)

@Serializable
data class ReorderPhotosRequestDto(val photoIds: List<String>)

@Serializable
data class PublicProfileDto(
    val id: String,
    val displayName: String,
    val age: Int,
    val gender: String,
    val interests: List<String> = emptyList(),
    val bio: String? = null,
    val city: String? = null,
    val isVerified: Boolean = false,
    val distanceKm: Int? = null,
    val isOnline: Boolean = false,
    val lastSeenAt: String? = null,
    val photos: List<PhotoDto> = emptyList(),
)

fun PublicProfileDto.toDomain() = PublicProfile(
    id = UUID.fromString(id),
    displayName = displayName,
    age = age,
    gender = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.OTHER),
    interests = interests,
    bio = bio,
    city = city,
    isVerified = isVerified,
    distanceKm = distanceKm,
    isOnline = isOnline,
    lastSeenAt = lastSeenAt?.let { Instant.parse(it) },
    photos = photos.map { it.toDomain() },
)
