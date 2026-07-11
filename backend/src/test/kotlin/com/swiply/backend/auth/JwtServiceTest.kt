package com.swiply.backend.auth

import com.swiply.backend.common.UnauthorizedException
import com.swiply.backend.common.UserRole
import com.swiply.backend.config.SwiplyProperties
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JwtServiceTest {

    private fun props(
        secret: String = "unit-test-secret-0123456789-0123456789-0123456789-0123456789",
        issuer: String = "swiply",
        accessTtl: Duration = Duration.ofMinutes(15),
    ) = SwiplyProperties(
        jwt = SwiplyProperties.Jwt(secret, issuer, accessTtl, Duration.ofDays(30)),
        media = SwiplyProperties.Media(
            "http://x", "http://x", "k", "s", "b",
            Duration.ofMinutes(30), 1, 6, 2048, listOf(512, 128),
        ),
        discovery = SwiplyProperties.Discovery(Duration.ofMinutes(10), 50, 300),
        limits = SwiplyProperties.Limits(100, 5, 15, 10, 10, 30),
        auth = SwiplyProperties.Auth(Duration.ofHours(24), Duration.ofHours(1)),
        admin = SwiplyProperties.Admin("a@a", "p"),
    )

    private val service = JwtService(props())

    @Test
    fun `генерация и разбор токена — roundtrip`() {
        val userId = UUID.randomUUID()
        val token = service.generateAccessToken(userId, "user@test", UserRole.USER)

        val principal = service.parse(token.token)

        assertEquals(userId, principal.userId)
        assertEquals("user@test", principal.email)
        assertEquals(UserRole.USER, principal.role)
        assertEquals(token.jti, principal.jti)
        assertTrue(principal.expiresAt.isAfter(java.time.Instant.now()))
    }

    @Nested
    inner class Отказы {

        @Test
        fun `подделанный токен отклоняется`() {
            val token = service.generateAccessToken(UUID.randomUUID(), "user@test", UserRole.USER)
            val tampered = token.token.dropLast(4) + "AAAA"
            assertThrows<UnauthorizedException> { service.parse(tampered) }
        }

        @Test
        fun `мусор вместо токена отклоняется`() {
            assertThrows<UnauthorizedException> { service.parse("not-a-jwt") }
        }

        @Test
        fun `истёкший токен отклоняется`() {
            val expired = JwtService(props(accessTtl = Duration.ofSeconds(-60)))
            val token = expired.generateAccessToken(UUID.randomUUID(), "user@test", UserRole.USER)
            assertThrows<UnauthorizedException> { expired.parse(token.token) }
        }

        @Test
        fun `чужой issuer отклоняется`() {
            val other = JwtService(props(issuer = "not-swiply"))
            val token = other.generateAccessToken(UUID.randomUUID(), "user@test", UserRole.USER)
            assertThrows<UnauthorizedException> { service.parse(token.token) }
        }

        @Test
        fun `токен на другом секрете отклоняется`() {
            val other = JwtService(props(secret = "another-secret-9876543210-9876543210-9876543210-9876543210"))
            val token = other.generateAccessToken(UUID.randomUUID(), "user@test", UserRole.USER)
            assertThrows<UnauthorizedException> { service.parse(token.token) }
        }
    }
}
