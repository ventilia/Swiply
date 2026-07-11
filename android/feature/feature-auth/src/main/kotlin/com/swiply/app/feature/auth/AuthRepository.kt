package com.swiply.app.feature.auth

import com.swiply.app.core.common.AppResult
import com.swiply.app.core.common.map
import com.swiply.app.core.common.onSuccess
import com.swiply.app.core.model.AuthSession
import com.swiply.app.core.model.Gender
import com.swiply.app.core.network.ApiCaller
import com.swiply.app.core.network.SessionManager
import com.swiply.app.core.network.api.AuthApi
import com.swiply.app.core.network.dto.ForgotPasswordRequestDto
import com.swiply.app.core.network.dto.LoginRequestDto
import com.swiply.app.core.network.dto.LogoutRequestDto
import com.swiply.app.core.network.dto.RegisterRequestDto
import com.swiply.app.core.network.dto.ResetPasswordRequestDto
import com.swiply.app.core.network.dto.VerifyEmailRequestDto
import com.swiply.app.core.network.dto.toDomain
import com.swiply.app.core.datastore.TokenStorage
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val apiCaller: ApiCaller,
    private val sessionManager: SessionManager,
    private val tokenStorage: TokenStorage,
) {

    suspend fun login(email: String, password: String): AppResult<AuthSession> =
        apiCaller.call { authApi.login(LoginRequestDto(email.trim(), password)).toDomain() }
            .onSuccess { sessionManager.onLogin(it) }

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        birthDate: LocalDate,
        gender: Gender,
        interestedIn: Set<Gender>,
        interests: List<String> = emptyList(),
    ): AppResult<AuthSession> =
        apiCaller.call {
            authApi.register(
                RegisterRequestDto(
                    email = email.trim(),
                    password = password,
                    displayName = displayName.trim(),
                    birthDate = birthDate.toString(),
                    gender = gender.name,
                    interestedIn = interestedIn.map { it.name },
                    interests = interests.ifEmpty { null },
                ),
            ).toDomain()
        }.onSuccess { sessionManager.onLogin(it) }

    suspend fun logout(): AppResult<Unit> {
        val refreshToken = tokenStorage.refreshToken()
        val result = apiCaller.call { authApi.logout(LogoutRequestDto(refreshToken)) }

        sessionManager.onLogout()
        return result.map { }
    }

    suspend fun forgotPassword(email: String): AppResult<Unit> =
        apiCaller.call { authApi.forgotPassword(ForgotPasswordRequestDto(email.trim())) }.map { }

    suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> =
        apiCaller.call { authApi.resetPassword(ResetPasswordRequestDto(token.trim(), newPassword)) }.map { }

    suspend fun verifyEmail(token: String): AppResult<Unit> =
        apiCaller.call { authApi.verifyEmail(VerifyEmailRequestDto(token.trim())) }.map { }
}
