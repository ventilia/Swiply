package com.swiply.backend.auth

import com.swiply.backend.common.UnauthorizedException
import com.swiply.backend.common.UserRole
import com.swiply.backend.config.SwiplyProperties
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

data class AccessToken(
    val token: String,
    val jti: String,
    val expiresAt: Instant,
)

data class AuthPrincipal(
    val userId: UUID,
    val email: String,
    val role: UserRole,
    val jti: String,
    val expiresAt: Instant,
)

@Service
class JwtService(props: SwiplyProperties) {

    private val jwtProps = props.jwt
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtProps.secret.toByteArray(Charsets.UTF_8))

    fun generateAccessToken(userId: UUID, email: String, role: UserRole): AccessToken {
        val now = Instant.now()
        val expiresAt = now.plus(jwtProps.accessTtl)
        val jti = UUID.randomUUID().toString()
        val token = Jwts.builder()
            .id(jti)
            .subject(userId.toString())
            .issuer(jwtProps.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .claim("email", email)
            .claim("role", role.name)
            .signWith(key)
            .compact()
        return AccessToken(token, jti, expiresAt)
    }


    fun parse(token: String): AuthPrincipal {
        val claims = try {
            Jwts.parser()
                .verifyWith(key)
                .requireIssuer(jwtProps.issuer)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: JwtException) {
            throw UnauthorizedException("Невалидный токен")
        } catch (e: IllegalArgumentException) {
            throw UnauthorizedException("Невалидный токен")
        }
        return AuthPrincipal(
            userId = UUID.fromString(claims.subject),
            email = claims["email"] as? String ?: "",
            role = runCatching { UserRole.valueOf(claims["role"] as String) }.getOrDefault(UserRole.USER),
            jti = claims.id ?: "",
            expiresAt = claims.expiration.toInstant(),
        )
    }
}
