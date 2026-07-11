package com.swiply.backend.auth

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID


@Service
class TokenBlacklistService(private val redis: StringRedisTemplate) {

    fun blacklistJti(jti: String, tokenExpiresAt: Instant) {
        val ttl = Duration.between(Instant.now(), tokenExpiresAt)
        if (ttl.isNegative || ttl.isZero) return
        redis.opsForValue().set("auth:blacklist:$jti", "1", ttl)
    }

    fun isJtiBlacklisted(jti: String): Boolean =
        jti.isNotEmpty() && redis.hasKey("auth:blacklist:$jti")


    fun markUserBlocked(userId: UUID, accessTtl: Duration) {
        redis.opsForValue().set("auth:blocked-user:$userId", "1", accessTtl)
    }

    fun unmarkUserBlocked(userId: UUID) {
        redis.delete("auth:blocked-user:$userId")
    }

    fun isUserBlocked(userId: UUID): Boolean =
        redis.hasKey("auth:blocked-user:$userId")
}
