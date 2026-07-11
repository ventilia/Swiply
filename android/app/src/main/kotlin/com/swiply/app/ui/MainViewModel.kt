package com.swiply.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiply.app.core.model.MatchCelebration
import com.swiply.app.feature.chat.ChatRepository
import com.swiply.app.feature.match.MatchRealtimeSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    matchRealtimeSync: MatchRealtimeSync,
) : ViewModel() {

    val unreadTotal: StateFlow<Int> = chatRepository.unreadTotal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val incomingMatches: SharedFlow<MatchCelebration> = matchRealtimeSync.incomingMatches

    init {
        // при входе в главный граф подтягиваем диалоги (переживает офлайн)
        viewModelScope.launch { chatRepository.refreshConversations() }
    }
}
