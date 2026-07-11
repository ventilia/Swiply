package com.swiply.app.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.model.ChatMessage
import com.swiply.app.core.model.Conversation
import com.swiply.app.core.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatInputState(
    val text: String = "",
    val isSendingImage: Boolean = false,
    val peerTyping: Boolean = false,
    val error: String? = null,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val chatRealtimeSync: ChatRealtimeSync,
    sessionManager: SessionManager,
) : ViewModel() {

    private val matchId: UUID = UUID.fromString(savedStateHandle.toRoute<ChatRoute>().matchId)

    val myUserId: UUID? = sessionManager.currentUserId

    private val conversationId = MutableStateFlow<String?>(null)


    val conversation: StateFlow<Conversation?> = conversationId
        .filterNotNull()
        .flatMapLatest { chatRepository.observeConversation(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages: Flow<PagingData<ChatMessage>> = conversationId
        .filterNotNull()
        .flatMapLatest { chatRepository.messagesPager(it) }
        .cachedIn(viewModelScope)

    private val _input = MutableStateFlow(ChatInputState())
    val input: StateFlow<ChatInputState> = _input

    private var typingResetJob: Job? = null
    private var meTypingJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = chatRepository.conversationByMatch(matchId)) {
                is AppResult.Success -> {
                    conversationId.value = result.data.id
                    chatRepository.markRead(result.data.id)
                }
                is AppResult.Failure -> _input.update { it.copy(error = result.error.message) }
            }
        }
        // индикатор «печатает…» от собеседника с автосбросом
        viewModelScope.launch {
            chatRealtimeSync.typing.collect { event ->
                if (event.conversationId != conversationId.value) return@collect
                _input.update { it.copy(peerTyping = event.isTyping) }
                typingResetJob?.cancel()
                if (event.isTyping) {
                    typingResetJob = launch {
                        delay(4_000)
                        _input.update { it.copy(peerTyping = false) }
                    }
                }
            }
        }
        // входящее сообщение при открытом чате — сразу прочитано
        viewModelScope.launch {
            chatRealtimeSync.incomingMessages.collect { message ->
                val id = conversationId.value ?: return@collect
                if (message.conversationId == id) {
                    chatRepository.markRead(id)
                }
            }
        }
    }

    fun onTextChanged(value: String) {
        _input.update { it.copy(text = value) }
        val id = conversationId.value ?: return
        // typing: true сразу, false — через паузу в наборе
        chatRepository.sendTyping(id, true)
        meTypingJob?.cancel()
        meTypingJob = viewModelScope.launch {
            delay(2_500)
            chatRepository.sendTyping(id, false)
        }
    }

    fun sendText() {
        val id = conversationId.value ?: return
        val text = _input.value.text.trim()
        if (text.isEmpty()) return
        _input.update { it.copy(text = "") }
        viewModelScope.launch {
            chatRepository.sendText(id, text)
            chatRepository.sendTyping(id, false)
        }
    }

    fun sendImage(uri: android.net.Uri) {
        val id = conversationId.value ?: return
        _input.update { it.copy(isSendingImage = true) }
        viewModelScope.launch {
            val result = chatRepository.sendImage(id, uri)
            _input.update {
                it.copy(
                    isSendingImage = false,
                    error = (result as? AppResult.Failure)?.error?.message,
                )
            }
        }
    }

    fun markReadNow() {
        val id = conversationId.value ?: return
        viewModelScope.launch { chatRepository.markRead(id) }
    }
}
