package com.swiply.app.feature.settings

import com.swiply.app.core.common.AppResult
import com.swiply.app.core.common.map
import com.swiply.app.core.database.SwiplyDatabase
import com.swiply.app.core.datastore.TokenStorage
import com.swiply.app.core.network.ApiCaller
import com.swiply.app.core.network.SessionManager
import com.swiply.app.core.network.api.AuthApi
import com.swiply.app.core.network.api.ProfileApi
import com.swiply.app.core.network.dto.DeleteAccountRequestDto
import com.swiply.app.core.network.dto.LogoutRequestDto
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class BlockedUser(
    val userId: UUID,
    val displayName: String?,
    val blockedAt: Instant,
)

@Singleton
class SettingsRepository @Inject constructor(
    private val authApi: AuthApi,
    private val profileApi: ProfileApi,
    private val apiCaller: ApiCaller,
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
    private val database: SwiplyDatabase,
) {

    /** Логаут: отзыв refresh на сервере (best effort) + полная локальная очистка */
    suspend fun logout() {
        val refreshToken = tokenStorage.refreshToken()
        apiCaller.call { authApi.logout(LogoutRequestDto(refreshToken)) }
        database.clearAll()
        sessionManager.onLogout()
    }

    suspend fun deleteAccount(password: String): AppResult<Unit> {
        val result = apiCaller.call { profileApi.deleteAccount(DeleteAccountRequestDto(password)) }
        if (result is AppResult.Success) {
            database.clearAll()
            sessionManager.onLogout()
        }
        return result.map { }
    }

    suspend fun blockedUsers(): AppResult<List<BlockedUser>> =
        apiCaller.call { profileApi.blockedUsers() }
            .map { list ->
                list.map {
                    BlockedUser(
                        userId = UUID.fromString(it.userId),
                        displayName = it.displayName,
                        blockedAt = Instant.parse(it.blockedAt),
                    )
                }
            }

    suspend fun unblock(userId: UUID): AppResult<Unit> =
        apiCaller.call { profileApi.unblock(userId.toString()) }.map { }
}
