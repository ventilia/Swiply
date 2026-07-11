package com.swiply.app.feature.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.database.dao.ConversationDao
import com.swiply.app.core.database.toDomain
import com.swiply.app.core.model.Conversation
import com.swiply.app.core.model.MatchItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val matchRealtimeSync: MatchRealtimeSync,
    conversationDao: ConversationDao,
) : ViewModel() {

    val matches: StateFlow<List<MatchItem>> = matchRepository.matches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Диалоги читаются из общего Room-кэша (его синкает feature-chat) */
    val conversations: StateFlow<List<Conversation>> = conversationDao.observeAll()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> = _likesCount

    init {
        refresh()
        viewModelScope.launch {
            matchRealtimeSync.likesChanged.collect { refreshLikesCount() }
        }
    }

    fun refresh() {
        viewModelScope.launch { matchRepository.refreshMatches() }
        viewModelScope.launch { refreshLikesCount() }
    }

    fun unmatch(matchId: UUID) {
        viewModelScope.launch { matchRepository.unmatch(matchId) }
    }

    private suspend fun refreshLikesCount() {
        when (val result = matchRepository.likesReceived()) {
            is AppResult.Success -> _likesCount.update { result.data.items.size }
            is AppResult.Failure -> Unit
        }
    }
}
