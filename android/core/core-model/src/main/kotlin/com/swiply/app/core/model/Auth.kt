package com.swiply.app.core.model

import java.time.Instant
import java.util.UUID

data class AuthUser(
    val id: UUID,
    val email: String,
    val emailVerified: Boolean,
)

data class AuthSession(
    val accessToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshToken: String,
    val user: AuthUser,
)
