package com.swiply.app.core.network

import com.swiply.app.core.datastore.TokenStorage
import com.swiply.app.core.model.AuthSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SessionState {
    data class LoggedIn(val userId: UUID) : SessionState
    data object LoggedOut : SessionState
}

/**
 * Владелец состояния сессии. Логин/логаут проходят через него, чтобы токены,
 * состояние навигации и WebSocket жили согласованно.
 */
@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage,
) {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<SessionState> = _state

    /** Сессия истекла (refresh отклонён) — UI показывает логин с объяснением */
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired

    val currentUserId: UUID?
        get() = (_state.value as? SessionState.LoggedIn)?.userId

    fun onLogin(session: AuthSession) {
        tokenStorage.save(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            userId = session.user.id.toString(),
        )
        _state.value = SessionState.LoggedIn(session.user.id)
    }

    /** Ротация токенов после refresh */
    fun onTokensRefreshed(session: AuthSession) {
        tokenStorage.save(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            userId = session.user.id.toString(),
        )
    }

    fun onLogout() {
        tokenStorage.clear()
        _state.value = SessionState.LoggedOut
    }

    fun onSessionExpired() {
        tokenStorage.clear()
        _state.value = SessionState.LoggedOut
        _sessionExpired.tryEmit(Unit)
    }

    private fun initialState(): SessionState {
        val userId = tokenStorage.userId()?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        return if (userId != null) SessionState.LoggedIn(userId) else SessionState.LoggedOut
    }
}
