package com.swiply.backend.common

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class RateLimiter(private val redis: StringRedisTemplate) {

    fun acquire(scope: String, key: String, limit: Int, window: Duration) {
        val redisKey = "ratelimit:$scope:$key"
        val count = redis.opsForValue().increment(redisKey) ?: 1
        if (count == 1L) {
            redis.expire(redisKey, window)
        }
        if (count > limit) {
            val ttl = redis.getExpire(redisKey)
            throw RateLimitedException(retryAfterSeconds = if (ttl > 0) ttl else window.seconds)
        }
    }


    fun remaining(scope: String, key: String, limit: Int): Long {
        val current = redis.opsForValue().get("ratelimit:$scope:$key")?.toLongOrNull() ?: 0
        return (limit - current).coerceAtLeast(0)
    }


    fun release(scope: String, key: String) {
        val redisKey = "ratelimit:$scope:$key"
        val value = redis.opsForValue().get(redisKey)?.toLongOrNull() ?: return
        if (value > 0) {
            redis.opsForValue().decrement(redisKey)
        }
    }
}
