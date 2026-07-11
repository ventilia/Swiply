package com.swiply.app.feature.profile

import android.net.Uri
import com.swiply.app.core.common.AppError
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.common.map
import com.swiply.app.core.common.media.ImageCompressor
import com.swiply.app.core.common.onSuccess
import com.swiply.app.core.database.dao.MyProfileDao
import com.swiply.app.core.database.entity.MyProfileEntity
import com.swiply.app.core.model.Gender
import com.swiply.app.core.model.MyProfile
import com.swiply.app.core.model.PublicProfile
import com.swiply.app.core.model.ReportReason
import com.swiply.app.core.network.ApiCaller
import com.swiply.app.core.network.api.AuthApi
import com.swiply.app.core.network.api.MediaApi
import com.swiply.app.core.network.api.ProfileApi
import com.swiply.app.core.network.api.ReportApi
import com.swiply.app.core.network.dto.VerifyEmailRequestDto
import com.swiply.app.core.network.dto.DeleteAccountRequestDto
import com.swiply.app.core.network.dto.MyProfileDto
import com.swiply.app.core.network.dto.ReorderPhotosRequestDto
import com.swiply.app.core.network.dto.ReportRequestDto
import com.swiply.app.core.network.dto.UpdateLocationRequestDto
import com.swiply.app.core.network.dto.UpdatePreferencesRequestDto
import com.swiply.app.core.network.dto.UpdateProfileRequestDto
import com.swiply.app.core.network.dto.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val authApi: AuthApi,
    private val mediaApi: MediaApi,
    private val reportApi: ReportApi,
    private val apiCaller: ApiCaller,
    private val myProfileDao: MyProfileDao,
    private val imageCompressor: ImageCompressor,
    private val json: Json,
) {

    /** Кэш профиля из Room — мгновенный старт, сеть обновляет в фоне */
    val myProfile: Flow<MyProfile?> = myProfileDao.observe().map { cached ->
        cached?.let { runCatching { json.decodeFromString<MyProfileDto>(it).toDomain() }.getOrNull() }
    }

    suspend fun refreshMyProfile(): AppResult<MyProfile> =
        apiCaller.call { profileApi.me() }
            .onSuccess { cache(it) }
            .map { it.toDomain() }

    suspend fun updateProfile(
        displayName: String? = null,
        bio: String? = null,
        gender: Gender? = null,
        interestedIn: Set<Gender>? = null,
        interests: List<String>? = null,
        city: String? = null,
    ): AppResult<MyProfile> =
        apiCaller.call {
            profileApi.updateProfile(
                UpdateProfileRequestDto(
                    displayName = displayName,
                    bio = bio,
                    gender = gender?.name,
                    interestedIn = interestedIn?.map { it.name },
                    interests = interests,
                    city = city,
                ),
            )
        }.onSuccess { cache(it) }.map { it.toDomain() }

    suspend fun updatePreferences(
        minAge: Int? = null,
        maxAge: Int? = null,
        maxDistanceKm: Int? = null,
        isIncognito: Boolean? = null,
        isDiscoverable: Boolean? = null,
    ): AppResult<MyProfile> =
        apiCaller.call {
            profileApi.updatePreferences(
                UpdatePreferencesRequestDto(
                    minAge = minAge,
                    maxAge = maxAge,
                    maxDistanceKm = maxDistanceKm,
                    isIncognito = isIncognito,
                    isDiscoverable = isDiscoverable,
                ),
            )
        }.onSuccess { cache(it) }.map { it.toDomain() }

    suspend fun updateLocation(latitude: Double, longitude: Double, city: String?): AppResult<Unit> =
        apiCaller.call {
            profileApi.updateLocation(UpdateLocationRequestDto(latitude, longitude, city))
        }.map { }

    /** Сжатие на клиенте (ТЗ) → multipart → бэкенд достраивает пайплайн */
    suspend fun uploadPhoto(uri: Uri): AppResult<MyProfile> {
        val bytes = imageCompressor.compress(uri)
            ?: return AppResult.Failure(AppError("BAD_IMAGE", "Не удалось прочитать изображение"))
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "photo.jpg",
            body = bytes.toRequestBody("image/jpeg".toMediaType()),
        )
        val upload = apiCaller.call { mediaApi.uploadPhoto(part) }
        if (upload is AppResult.Failure) return AppResult.Failure(upload.error)
        return refreshMyProfile()
    }

    suspend fun deletePhoto(photoId: UUID): AppResult<MyProfile> {
        val result = apiCaller.call { mediaApi.deletePhoto(photoId.toString()) }
        if (result is AppResult.Failure) return AppResult.Failure(result.error)
        return refreshMyProfile()
    }

    suspend fun reorderPhotos(photoIds: List<UUID>): AppResult<MyProfile> {
        val result = apiCaller.call {
            mediaApi.reorderPhotos(ReorderPhotosRequestDto(photoIds.map { it.toString() }))
        }
        if (result is AppResult.Failure) return AppResult.Failure(result.error)
        return refreshMyProfile()
    }

    suspend fun publicProfile(userId: UUID): AppResult<PublicProfile> =
        apiCaller.call { profileApi.publicProfile(userId.toString()).toDomain() }

    suspend fun blockUser(userId: UUID): AppResult<Unit> =
        apiCaller.call { profileApi.block(userId.toString()) }.map { }

    suspend fun reportUser(userId: UUID, reason: ReportReason, description: String?): AppResult<Unit> =
        apiCaller.call {
            reportApi.submitReport(
                ReportRequestDto(
                    targetUserId = userId.toString(),
                    reason = reason.name,
                    description = description?.trim()?.ifBlank { null },
                ),
            )
        }.map { }

    suspend fun deleteAccount(password: String): AppResult<Unit> =
        apiCaller.call { profileApi.deleteAccount(DeleteAccountRequestDto(password)) }.map { }

    suspend fun verifyEmail(token: String): AppResult<Unit> =
        apiCaller.call { authApi.verifyEmail(VerifyEmailRequestDto(token.trim())) }.map { }

    suspend fun resendVerification(): AppResult<Unit> =
        apiCaller.call { authApi.resendVerification() }.map { }

    private suspend fun cache(dto: MyProfileDto) {
        myProfileDao.upsert(MyProfileEntity(json = json.encodeToString(MyProfileDto.serializer(), dto)))
    }
}
