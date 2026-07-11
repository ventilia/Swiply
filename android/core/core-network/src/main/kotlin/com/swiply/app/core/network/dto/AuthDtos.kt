package com.swiply.app.core.network.dto

import com.swiply.app.core.model.AuthSession
import com.swiply.app.core.model.AuthUser
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String,
    /** ISO-8601: 1995-06-15 */
    val birthDate: String,
    val gender: String,
    val interestedIn: List<String>,
    val interests: List<String>? = null,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequestDto(val refreshToken: String)

@Serializable
data class LogoutRequestDto(val refreshToken: String?)

@Serializable
data class VerifyEmailRequestDto(val token: String)

@Serializable
data class ForgotPasswordRequestDto(val email: String)

@Serializable
data class ResetPasswordRequestDto(val token: String, val newPassword: String)

@Serializable
data class AuthUserDto(
    val id: String,
    val email: String,
    val emailVerified: Boolean = false,
)

@Serializable
data class TokenResponseDto(
    val accessToken: String,
    val accessTokenExpiresAt: String,
    val refreshToken: String,
    val user: AuthUserDto,
)

fun TokenResponseDto.toDomain() = AuthSession(
    accessToken = accessToken,
    accessTokenExpiresAt = Instant.parse(accessTokenExpiresAt),
    refreshToken = refreshToken,
    user = AuthUser(
        id = UUID.fromString(user.id),
        email = user.email,
        emailVerified = user.emailVerified,
    ),
)
