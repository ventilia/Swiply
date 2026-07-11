package com.swiply.backend.auth

import com.swiply.backend.common.Gender
import com.swiply.backend.common.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class RegisterRequest(
    @field:NotBlank @field:Email @field:Size(max = 254)
    val email: String,

    @field:NotBlank @field:Size(min = 8, max = 72)
    val password: String,

    @field:NotBlank @field:Size(min = 2, max = 40)
    val displayName: String,

    @field:NotNull
    val birthDate: LocalDate,

    @field:NotNull
    val gender: Gender,

    @field:NotEmpty
    val interestedIn: Set<Gender>,


    val interests: List<String>? = null,
)

data class LoginRequest(
    @field:NotBlank @field:Email
    val email: String,

    @field:NotBlank
    val password: String,
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)

data class LogoutRequest(
    val refreshToken: String? = null,
)

data class VerifyEmailRequest(
    @field:NotBlank
    val token: String,
)

data class ForgotPasswordRequest(
    @field:NotBlank @field:Email
    val email: String,
)

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,

    @field:NotBlank @field:Size(min = 8, max = 72)
    val newPassword: String,
)

data class AuthUserView(
    val id: UUID,
    val email: String,
    val emailVerified: Boolean,
    val role: UserRole,
)

data class TokenResponse(
    val accessToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshToken: String,
    val user: AuthUserView,
)

data class OkResponse(val ok: Boolean = true)
