package com.swiply.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "swiply")
data class SwiplyProperties(
    val jwt: Jwt,
    val media: Media,
    val discovery: Discovery,
    val limits: Limits,
    val auth: Auth,
    val admin: Admin,
    val seed: Seed = Seed(),
) {
    data class Jwt(
        val secret: String,
        val issuer: String,
        val accessTtl: Duration,
        val refreshTtl: Duration,
    )

    data class Media(
        val endpoint: String,
        val publicEndpoint: String,
        val accessKey: String,
        val secretKey: String,
        val photosBucket: String,
        val presignTtl: Duration,
        val maxPhotoBytes: Long,
        val maxPhotosPerUser: Int,
        val maxDimensionPx: Int,
        val thumbnailSizes: List<Int>,
    )

    data class Discovery(
        val cacheTtl: Duration,
        val pageSizeMax: Int,
        val radiusKmMax: Int,
    )

    data class Limits(
        val swipesPerDay: Int,
        val superlikesPerDay: Int,
        val messagesPer10s: Int,
        val reportsPerDay: Int,
        val loginPerMinute: Int,
        val registerPerHour: Int,
    )

    data class Auth(
        val emailVerificationTtl: Duration,
        val passwordResetTtl: Duration,
        val logEmailTokens: Boolean = false,
    )

    data class Admin(
        val bootstrapEmail: String,
        val bootstrapPassword: String,
    )

    data class Seed(
        val enabled: Boolean = false,
    )
}
