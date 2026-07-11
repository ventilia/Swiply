package com.swiply.app.feature.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.common.AppError
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.model.Candidate
import com.swiply.app.core.model.MatchCelebration
import com.swiply.app.core.model.SwipeAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoveryUiState(
    /** Верх стека — deck.first() */
    val deck: List<Candidate> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** У аккаунта нет локации на бэке и получить её не удалось — нужна геолокация */
    val needsLocation: Boolean = false,
    val remainingLikes: Long? = null,
    val canUndo: Boolean = false,
    val rateLimited: Boolean = false,
    val exhausted: Boolean = false,
)

sealed interface DiscoveryEvent {
    data class MatchCreated(val celebration: MatchCelebration) : DiscoveryEvent
}

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val repository: DiscoveryRepository,
    private val locationUpdater: LocationUpdater,
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoveryUiState())
    val state: StateFlow<DiscoveryUiState> = _state

    private val _events = MutableSharedFlow<DiscoveryEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<DiscoveryEvent> = _events

    /** Последний свайпнутый — для мгновенного undo без перезагрузки ленты */
    private var lastSwiped: Candidate? = null
    private var started = false

    /**
     * Вызывается при входе на экран. Лента грузится СРАЗУ — на бэке уже есть
     * последняя локация (сид/прошлая сессия), поэтому не ждём GPS-фикс.
     */
    fun start() {
        if (started) return
        started = true
        load()
    }

    /**
     * Результат запроса разрешения на гео. Лента уже грузится в [start];
     * здесь только фоново обновляем локацию (не блокируя экран) и, если лента
     * пустовала из-за отсутствия локации, повторяем загрузку.
     */
    fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) return
        viewModelScope.launch {
            if (locationUpdater.updateLocation() is AppResult.Success && _state.value.deck.isEmpty()) {
                load()
            }
        }
    }

    fun retry() = load()

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null, needsLocation = false) }
        viewModelScope.launch {
            when (val result = repository.loadFirstPage()) {
                is AppResult.Success -> applyDeck(result.data)
                is AppResult.Failure -> handleLoadFailure(result.error)
            }
        }
    }

    private suspend fun handleLoadFailure(error: AppError) {
        if (error.code == "NO_LOCATION") {
            // Новый аккаунт без локации: пробуем определить сами и повторяем
            if (locationUpdater.updateLocation() is AppResult.Success) {
                when (val retry = repository.loadFirstPage()) {
                    is AppResult.Success -> applyDeck(retry.data)
                    is AppResult.Failure -> _state.update {
                        it.copy(isLoading = false, needsLocation = retry.error.code == "NO_LOCATION", error = retry.error.message.takeIf { retry.error.code != "NO_LOCATION" })
                    }
                }
            } else {
                _state.update { it.copy(isLoading = false, needsLocation = true) }
            }
        } else {
            _state.update { it.copy(isLoading = false, error = error.message) }
        }
    }

    private fun applyDeck(deck: List<Candidate>) {
        _state.update {
            it.copy(deck = deck, isLoading = false, exhausted = deck.isEmpty() && !repository.hasMore)
        }
    }

    fun onSwiped(candidate: Candidate, action: SwipeAction) {
        // optimistic: карточка уже улетела с экрана
        _state.update { s ->
            s.copy(
                deck = s.deck.filterNot { it.userId == candidate.userId },
                canUndo = action != SwipeAction.DISLIKE || s.canUndo,
            )
        }
        lastSwiped = candidate

        viewModelScope.launch {
            when (val result = repository.swipe(candidate.userId, action)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            remainingLikes = result.data.remainingLikes ?: it.remainingLikes,
                            canUndo = !result.data.matched,
                            rateLimited = false,
                        )
                    }
                    if (result.data.matched && result.data.matchId != null) {
                        lastSwiped = null
                        _events.tryEmit(
                            DiscoveryEvent.MatchCreated(
                                MatchCelebration(
                                    matchId = result.data.matchId!!,
                                    otherUserId = candidate.userId,
                                    otherDisplayName = candidate.displayName,
                                    otherThumbUrl = candidate.photos.firstOrNull()?.thumbUrl
                                        ?: candidate.photos.firstOrNull()?.url,
                                ),
                            ),
                        )
                    }
                }
                is AppResult.Failure -> {
                    if (result.error.code == "RATE_LIMITED") {
                        // лайк не засчитан — возвращаем карточку в стек
                        _state.update {
                            it.copy(deck = listOf(candidate) + it.deck, rateLimited = true, canUndo = false)
                        }
                    }
                    // прочие ошибки — молча: свайп идемпотентен, при рестарте лента честная
                }
            }
            prefetchIfNeeded()
        }
    }

    fun undo() {
        val candidate = lastSwiped ?: return
        viewModelScope.launch {
            when (repository.undoLastSwipe()) {
                is AppResult.Success -> {
                    _state.update { it.copy(deck = listOf(candidate) + it.deck, canUndo = false) }
                    lastSwiped = null
                }
                is AppResult.Failure -> _state.update { it.copy(canUndo = false) }
            }
        }
    }

    fun dismissRateLimit() = _state.update { it.copy(rateLimited = false) }

    private suspend fun prefetchIfNeeded() {
        val s = _state.value
        if (s.deck.size >= 4 || !repository.hasMore || s.isLoading) {
            if (s.deck.isEmpty() && !repository.hasMore) {
                _state.update { it.copy(exhausted = true) }
            }
            return
        }
        when (val result = repository.loadNextPage()) {
            is AppResult.Success -> _state.update { current ->
                val known = current.deck.map { it.userId }.toSet()
                val fresh = result.data.filterNot { it.userId in known }
                current.copy(
                    deck = current.deck + fresh,
                    exhausted = (current.deck + fresh).isEmpty() && !repository.hasMore,
                )
            }
            is AppResult.Failure -> Unit // тихий префетч
        }
    }
}
