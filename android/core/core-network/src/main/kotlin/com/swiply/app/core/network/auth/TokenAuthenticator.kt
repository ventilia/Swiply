package com.swiply.app.core.network.auth

import com.swiply.app.core.datastore.TokenStorage
import com.swiply.app.core.network.SessionManager
import com.swiply.app.core.network.api.TokenRefreshApi
import com.swiply.app.core.network.dto.RefreshRequestDto
import com.swiply.app.core.network.dto.toDomain
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Прозрачный refresh access-токена на 401.
 *
 * Refresh идёт через отдельный Retrofit без Authenticator'а (иначе цикл).
 * synchronized + повторная проверка токена защищают от параллельных refresh'ей
 * из нескольких одновременных запросов.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
    private val refreshApi: Provider<TokenRefreshApi>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // refresh не помог дважды — сдаёмся
        if (response.priorResponse != null) return null

        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            ?: return null

        synchronized(this) {
            val currentToken = tokenStorage.accessToken() ?: return null

            // другой поток уже обновил токен, пока мы ждали монитор
            if (currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = tokenStorage.refreshToken() ?: return null
            val newSession = runCatching {
                runBlocking { refreshApi.get().refresh(RefreshRequestDto(refreshToken)).toDomain() }
            }.getOrNull()

            if (newSession == null) {
                // refresh отклонён: сессия мертва
                sessionManager.onSessionExpired()
                return null
            }

            sessionManager.onTokensRefreshed(newSession)
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${newSession.accessToken}")
                .build()
        }
    }
}
