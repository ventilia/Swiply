package com.swiply.backend.auth

import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.ConflictException
import com.swiply.backend.common.ForbiddenException
import com.swiply.backend.common.UnauthorizedException
import com.swiply.backend.common.RateLimiter
import com.swiply.backend.common.UserStatus
import com.swiply.backend.config.SwiplyProperties
import com.swiply.backend.profile.Profile
import com.swiply.backend.profile.ProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.util.Base64
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val blacklist: TokenBlacklistService,
    private val emailSender: EmailSender,
    private val rateLimiter: RateLimiter,
    private val props: SwiplyProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val secureRandom = SecureRandom()

    @Transactional
    fun register(request: RegisterRequest, clientIp: String): TokenResponse {
        rateLimiter.acquire("register", clientIp, props.limits.registerPerHour, Duration.ofHours(1))

        val email = request.email.trim().lowercase()
        val age = Period.between(request.birthDate, LocalDate.now()).years
        if (age < 18) {
            throw BadRequestException("UNDERAGE", "Регистрация доступна только с 18 лет")
        }
        if (age > 120) {
            throw BadRequestException("BAD_BIRTH_DATE", "Некорректная дата рождения")
        }
        if (userRepository.existsByEmail(email)) {
            throw ConflictException("EMAIL_TAKEN", "Пользователь с такой почтой уже существует")
        }

        val user = userRepository.save(
            User(
                email = email,
                passwordHash = passwordEncoder.encode(request.password),
            ),
        )
        profileRepository.save(
            Profile(
                userId = user.id,
                displayName = request.displayName.trim(),
                birthDate = request.birthDate,
                gender = request.gender,
                interestedIn = request.interestedIn.map { it.name }.toMutableList(),
                interests = com.swiply.backend.profile.Interests.sanitize(request.interests ?: emptyList()),
            ),
        )
        sendVerificationEmail(user)
        return issueTokens(user, deviceInfo = null)
    }

    @Transactional
    fun login(request: LoginRequest, clientIp: String, deviceInfo: String?): TokenResponse {
        rateLimiter.acquire("login-ip", clientIp, props.limits.loginPerMinute, Duration.ofMinutes(1))
        val email = request.email.trim().lowercase()
        rateLimiter.acquire("login-email", email, props.limits.loginPerMinute, Duration.ofMinutes(1))

        val user = userRepository.findByEmail(email)
            ?: throw UnauthorizedException("Неверная почта или пароль")
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Неверная почта или пароль")
        }
        ensureLoginAllowed(user)

        user.lastActiveAt = Instant.now()
        return issueTokens(user, deviceInfo)
    }

    @Transactional
    fun refresh(request: RefreshRequest): TokenResponse {
        val tokenHash = sha256(request.refreshToken)
        val stored = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw UnauthorizedException("Refresh-токен не найден")
        if (stored.revoked || stored.expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedException("Refresh-токен истёк или отозван")
        }
        val user = userRepository.findById(stored.userId)
            .orElseThrow { UnauthorizedException("Пользователь не найден") }
        ensureLoginAllowed(user)


        stored.revoked = true
        refreshTokenRepository.save(stored)
        user.lastActiveAt = Instant.now()
        return issueTokens(user, stored.deviceInfo)
    }

    @Transactional
    fun logout(request: LogoutRequest, principal: AuthPrincipal?) {
        request.refreshToken?.let { raw ->
            refreshTokenRepository.findByTokenHash(sha256(raw))?.let {
                it.revoked = true
                refreshTokenRepository.save(it)
            }
        }
        principal?.let { blacklist.blacklistJti(it.jti, it.expiresAt) }
    }

    @Transactional
    fun verifyEmail(request: VerifyEmailRequest) {
        val token = emailVerificationTokenRepository.findByTokenHash(sha256(request.token))
            ?: throw BadRequestException("BAD_TOKEN", "Токен подтверждения не найден")
        if (token.usedAt != null || token.expiresAt.isBefore(Instant.now())) {
            throw BadRequestException("TOKEN_EXPIRED", "Токен уже использован или истёк")
        }
        val user = userRepository.findById(token.userId)
            .orElseThrow { BadRequestException("BAD_TOKEN", "Пользователь не найден") }
        user.emailVerified = true
        token.usedAt = Instant.now()
        userRepository.save(user)
        emailVerificationTokenRepository.save(token)
    }

    @Transactional
    fun forgotPassword(request: ForgotPasswordRequest, clientIp: String) {
        rateLimiter.acquire("forgot-password", clientIp, 5, Duration.ofHours(1))
        val user = userRepository.findByEmail(request.email.trim().lowercase())
        if (user == null || user.status == UserStatus.DELETED) return

        val raw = randomToken()
        passwordResetTokenRepository.save(
            PasswordResetToken(
                userId = user.id,
                tokenHash = sha256(raw),
                expiresAt = Instant.now().plus(props.auth.passwordResetTtl),
            ),
        )
        emailSender.sendPasswordReset(user.email, raw)
    }

    @Transactional
    fun resetPassword(request: ResetPasswordRequest) {
        val token = passwordResetTokenRepository.findByTokenHash(sha256(request.token))
            ?: throw BadRequestException("BAD_TOKEN", "Токен сброса не найден")
        if (token.usedAt != null || token.expiresAt.isBefore(Instant.now())) {
            throw BadRequestException("TOKEN_EXPIRED", "Токен уже использован или истёк")
        }
        val user = userRepository.findById(token.userId)
            .orElseThrow { BadRequestException("BAD_TOKEN", "Пользователь не найден") }

        user.passwordHash = passwordEncoder.encode(request.newPassword)
        token.usedAt = Instant.now()
        userRepository.save(user)
        passwordResetTokenRepository.save(token)

        refreshTokenRepository.revokeAllForUser(user.id)
    }

    @Transactional
    fun resendVerification(userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow { UnauthorizedException() }
        if (user.emailVerified) {
            throw ConflictException("ALREADY_VERIFIED", "Почта уже подтверждена")
        }
        sendVerificationEmail(user)
    }

    private fun ensureLoginAllowed(user: User) {
        when (user.status) {
            UserStatus.BANNED -> throw ForbiddenException("ACCOUNT_BANNED", "Аккаунт заблокирован")
            UserStatus.SUSPENDED -> throw ForbiddenException("ACCOUNT_SUSPENDED", "Аккаунт временно приостановлен")
            UserStatus.DELETED -> throw UnauthorizedException("Неверная почта или пароль")
            UserStatus.ACTIVE -> Unit
        }
    }

    private fun sendVerificationEmail(user: User) {
        val raw = randomToken()
        emailVerificationTokenRepository.save(
            EmailVerificationToken(
                userId = user.id,
                tokenHash = sha256(raw),
                expiresAt = Instant.now().plus(props.auth.emailVerificationTtl),
            ),
        )
        emailSender.sendEmailVerification(user.email, raw)
    }

    private fun issueTokens(user: User, deviceInfo: String?): TokenResponse {
        val access = jwtService.generateAccessToken(user.id, user.email, user.role)
        val rawRefresh = randomToken()
        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id,
                tokenHash = sha256(rawRefresh),
                deviceInfo = deviceInfo?.take(256),
                expiresAt = Instant.now().plus(props.jwt.refreshTtl),
            ),
        )
        return TokenResponse(
            accessToken = access.token,
            accessTokenExpiresAt = access.expiresAt,
            refreshToken = rawRefresh,
            user = AuthUserView(user.id, user.email, user.emailVerified, user.role),
        )
    }

    private fun randomToken(): String {
        val bytes = ByteArray(48)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
