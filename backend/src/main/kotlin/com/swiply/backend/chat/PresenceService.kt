package com.swiply.backend.chat

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

data class Presence(val online: Boolean, val lastSeenAt: Instant?)


@Service
class PresenceService(private val redis: StringRedisTemplate) {

    companion object {
        /** TTL онлайн-маячка; heartbeat клиента должен приходить чаще. */
        val ONLINE_TTL: Duration = Duration.ofSeconds(90)
    }

    fun markOnline(userId: UUID) {
        redis.opsForValue().set("presence:online:$userId", "1", ONLINE_TTL)
    }

    fun markOffline(userId: UUID) {
        redis.delete("presence:online:$userId")
        touchLastSeen(userId)
    }

    fun touchLastSeen(userId: UUID) {
        redis.opsForValue().set("presence:lastseen:$userId", Instant.now().toEpochMilli().toString())
    }

    fun isOnline(userId: UUID): Boolean = redis.hasKey("presence:online:$userId")

    fun presenceOf(userId: UUID): Presence {
        val online = isOnline(userId)
        val lastSeen = redis.opsForValue().get("presence:lastseen:$userId")
            ?.toLongOrNull()
            ?.let { Instant.ofEpochMilli(it) }
        return Presence(online, lastSeen)
    }
}
