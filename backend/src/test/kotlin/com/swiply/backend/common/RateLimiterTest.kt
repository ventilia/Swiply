package com.swiply.backend.common

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import kotlin.test.assertEquals

class RateLimiterTest {

    private val valueOps: ValueOperations<String, String> = mockk(relaxed = true)
    private val redis: StringRedisTemplate = mockk(relaxed = true) {
        every { opsForValue() } returns valueOps
    }
    private val limiter = RateLimiter(redis)

    @Test
    fun `первый инкремент ставит TTL окна`() {
        every { valueOps.increment("ratelimit:login:1.2.3.4") } returns 1L

        limiter.acquire("login", "1.2.3.4", limit = 5, window = Duration.ofMinutes(1))

        verify(exactly = 1) { redis.expire("ratelimit:login:1.2.3.4", Duration.ofMinutes(1)) }
    }

    @Test
    fun `в пределах лимита пропускает без TTL-обновления`() {
        every { valueOps.increment(any<String>()) } returns 3L

        limiter.acquire("login", "key", limit = 5, window = Duration.ofMinutes(1))

        verify(exactly = 0) { redis.expire(any<String>(), any<Duration>()) }
    }

    @Test
    fun `сверх лимита бросает RateLimitedException с Retry-After`() {
        every { valueOps.increment(any<String>()) } returns 6L
        every { redis.getExpire(any<String>()) } returns 42L

        val ex = assertThrows<RateLimitedException> {
            limiter.acquire("login", "key", limit = 5, window = Duration.ofMinutes(1))
        }
        assertEquals(42L, ex.retryAfterSeconds)
    }

    @Test
    fun `remaining не уходит в минус`() {
        every { valueOps.get("ratelimit:swipe:u1") } returns "120"
        assertEquals(0, limiter.remaining("swipe", "u1", limit = 100))
    }

    @Test
    fun `release декрементит только существующий счётчик`() {
        every { valueOps.get("ratelimit:swipe:u1") } returns null
        limiter.release("swipe", "u1")
        verify(exactly = 0) { valueOps.decrement(any<String>()) }

        every { valueOps.get("ratelimit:swipe:u1") } returns "3"
        limiter.release("swipe", "u1")
        verify(exactly = 1) { valueOps.decrement("ratelimit:swipe:u1") }
    }
}
