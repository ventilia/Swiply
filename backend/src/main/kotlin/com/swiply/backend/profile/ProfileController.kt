package com.swiply.backend.profile

import com.swiply.backend.auth.OkResponse
import com.swiply.backend.common.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class DeleteAccountRequest(
    @field:NotBlank
    val password: String,
)

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Profile", description = "Профиль и настройки поиска")
class ProfileController(
    private val profileService: ProfileService,
    private val accountDeletionService: AccountDeletionService,
) {

    @GetMapping("/me")
    @Operation(summary = "Мой профиль")
    suspend fun me(): MyProfileResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { profileService.getMe(userId) }
    }

    @PutMapping("/me")
    @Operation(summary = "Обновление профиля (частичное: null-поля не меняются)")
    suspend fun updateMe(@Valid @RequestBody request: UpdateProfileRequest): MyProfileResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { profileService.updateMe(userId, request) }
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Настройки поиска: возраст, дистанция, видимость, инкогнито")
    suspend fun updatePreferences(@Valid @RequestBody request: UpdatePreferencesRequest): MyProfileResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { profileService.updatePreferences(userId, request) }
    }

    @PutMapping("/me/location")
    @Operation(summary = "Обновление геопозиции (точные координаты не покидают сервер)")
    suspend fun updateLocation(@Valid @RequestBody request: UpdateLocationRequest): OkResponse {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { profileService.updateLocation(userId, request) }
        return OkResponse()
    }

    @GetMapping("/{id}")
    @Operation(summary = "Публичный профиль пользователя")
    suspend fun publicProfile(@PathVariable id: UUID): PublicProfileResponse {
        val viewerId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { profileService.getPublicProfile(viewerId, id) }
    }

    @DeleteMapping("/me")
    @Operation(summary = "Удаление аккаунта: анонимизация данных, стирание фото, разрыв сессий")
    suspend fun deleteAccount(@Valid @RequestBody request: DeleteAccountRequest): OkResponse {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { accountDeletionService.deleteAccount(userId, request.password) }
        return OkResponse()
    }
}
