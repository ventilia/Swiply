package com.swiply.app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.swiply.app.core.common.ApplicationScope
import com.swiply.app.core.network.SessionManager
import com.swiply.app.core.network.SessionState
import com.swiply.app.core.network.realtime.RealtimeClient
import com.swiply.app.feature.chat.ChatRealtimeSync
import com.swiply.app.feature.match.MatchRealtimeSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Оркестратор процесса: WebSocket живёт, пока пользователь залогинен
 * и приложение на переднем плане; realtime-синки подписаны всегда.
 */
@Singleton
class AppBootstrap @Inject constructor(
    private val realtimeClient: RealtimeClient,
    private val sessionManager: SessionManager,
    private val chatRealtimeSync: ChatRealtimeSync,
    private val matchRealtimeSync: MatchRealtimeSync,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val foreground = MutableStateFlow(false)

    fun start() {
        chatRealtimeSync.start()
        matchRealtimeSync.start()

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                foreground.value = true
            }

            override fun onStop(owner: LifecycleOwner) {
                foreground.value = false
            }
        })

        scope.launch {
            combine(foreground, sessionManager.state) { fg, session ->
                fg && session is SessionState.LoggedIn
            }
                .distinctUntilChanged()
                .collect { shouldConnect ->
                    if (shouldConnect) realtimeClient.start() else realtimeClient.stop()
                }
        }
    }
}
