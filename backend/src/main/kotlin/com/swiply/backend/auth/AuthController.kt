package com.swiply.backend.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


fun clientIp(request: HttpServletRequest): String =
    request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
        ?: request.remoteAddr
        ?: "unknown"

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Регистрация, вход, JWT")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация (только 18+), сразу возвращает токены")
    suspend fun register(
        @Valid @RequestBody request: RegisterRequest,
        http: HttpServletRequest,
    ): TokenResponse {
        val ip = clientIp(http)
        return withContext(Dispatchers.IO) { authService.register(request, ip) }
    }

    @PostMapping("/login")
    @Operation(summary = "Вход по почте и паролю")
    suspend fun login(
        @Valid @RequestBody request: LoginRequest,
        http: HttpServletRequest,
    ): TokenResponse {
        val ip = clientIp(http)
        val device = http.getHeader("User-Agent")
        return withContext(Dispatchers.IO) { authService.login(request, ip, device) }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обмен refresh-токена на новую пару (ротация)")
    suspend fun refresh(@Valid @RequestBody request: RefreshRequest): TokenResponse =
        withContext(Dispatchers.IO) { authService.refresh(request) }

    @PostMapping("/logout")
    @Operation(summary = "Отзыв refresh-токена и текущего access-токена")
    suspend fun logout(@RequestBody(required = false) request: LogoutRequest?): OkResponse {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? AuthPrincipal
        withContext(Dispatchers.IO) { authService.logout(request ?: LogoutRequest(), principal) }
        return OkResponse()
    }

    @PostMapping("/email/verify")
    @Operation(summary = "Подтверждение почты по токену из письма")
    suspend fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest): OkResponse {
        withContext(Dispatchers.IO) { authService.verifyEmail(request) }
        return OkResponse()
    }

    @PostMapping("/email/resend")
    @Operation(summary = "Повторно отправить письмо подтверждения (нужен токен авторизации)")
    suspend fun resendVerification(): OkResponse {
        val userId = com.swiply.backend.common.SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { authService.resendVerification(userId) }
        return OkResponse()
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "Запрос сброса пароля (всегда 200, чтобы не раскрывать наличие аккаунта)")
    suspend fun forgotPassword(
        @Valid @RequestBody request: ForgotPasswordRequest,
        http: HttpServletRequest,
    ): OkResponse {
        val ip = clientIp(http)
        withContext(Dispatchers.IO) { authService.forgotPassword(request, ip) }
        return OkResponse()
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Установка нового пароля по токену сброса")
    suspend fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): OkResponse {
        withContext(Dispatchers.IO) { authService.resetPassword(request) }
        return OkResponse()
    }
}
