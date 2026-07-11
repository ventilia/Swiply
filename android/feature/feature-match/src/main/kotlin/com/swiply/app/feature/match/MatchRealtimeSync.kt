package com.swiply.app.feature.match

import com.swiply.app.core.common.ApplicationScope
import com.swiply.app.core.database.dao.ConversationDao
import com.swiply.app.core.database.dao.MatchDao
import com.swiply.app.core.model.MatchCelebration
import com.swiply.app.core.network.realtime.RealtimeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Держит мэтчи в актуальном состоянии по WebSocket-событиям:
 * match.created / match.removed / presence.changed / like.received.
 */
@Singleton
class MatchRealtimeSync @Inject constructor(
    private val realtimeClient: RealtimeClient,
    private val matchRepository: MatchRepository,
    private val matchDao: MatchDao,
    private val conversationDao: ConversationDao,
    @ApplicationScope private val scope: CoroutineScope,
) {

    /** Входящий мэтч (нас лайкнули в ответ) — app показывает celebration */
    private val _incomingMatches = MutableSharedFlow<MatchCelebration>(extraBufferCapacity = 4)
    val incomingMatches: SharedFlow<MatchCelebration> = _incomingMatches

    /** Сигнал «лайки изменились» — экран лайков может обновиться */
    private val _likesChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val likesChanged: SharedFlow<Unit> = _likesChanged

    fun start() {
        scope.launch {
            realtimeClient.events.collect { event ->
                when (event.type) {
                    "match.created" -> {
                        matchRepository.refreshMatches()
                        val matchId = event.payload["matchId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                        val userId = event.payload["userId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                        if (matchId != null && userId != null) {
                            _incomingMatches.tryEmit(
                                MatchCelebration(
                                    matchId = matchId,
                                    otherUserId = userId,
                                    otherDisplayName = event.payload["displayName"].orEmpty(),
                                    otherThumbUrl = event.payload["thumbUrl"],
                                ),
                            )
                        }
                    }
                    "match.removed" -> {
                        event.payload["matchId"]?.let { matchDao.delete(it) }
                        matchRepository.refreshMatches()
                    }
                    "presence.changed" -> {
                        val userId = event.payload["userId"] ?: return@collect
                        val online = event.payload["online"]?.toBoolean() ?: false
                        matchDao.updatePresence(userId, online)
                        conversationDao.updatePresence(userId, online)
                    }
                    "like.received" -> _likesChanged.tryEmit(Unit)
                }
            }
        }
        scope.launch {
            realtimeClient.reconnected.collect {
                matchRepository.refreshMatches()
            }
        }
    }
}
