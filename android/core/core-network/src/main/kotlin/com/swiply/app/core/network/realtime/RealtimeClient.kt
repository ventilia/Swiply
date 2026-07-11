package com.swiply.app.core.network.realtime

import com.swiply.app.core.common.ApplicationScope
import com.swiply.app.core.datastore.TokenStorage
import com.swiply.app.core.model.ChatMessage
import com.swiply.app.core.model.RealtimeEvent
import com.swiply.app.core.network.dto.ChatSendPayloadDto
import com.swiply.app.core.network.dto.MessageDto
import com.swiply.app.core.network.dto.ReadPayloadDto
import com.swiply.app.core.network.dto.RealtimeEventDto
import com.swiply.app.core.network.dto.TypingPayloadDto
import com.swiply.app.core.network.dto.toDomain
import com.swiply.app.core.network.stomp.StompClient
import com.swiply.app.core.network.stomp.StompEvent
import com.swiply.app.core.network.stomp.StompSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Realtime-шина приложения: держит STOMP-подключение, пока пользователь
 * залогинен и приложение на переднем плане, с автопереподключением.
 *
 * WebSocket — только «живая» доставка: после reconnect клиент дотягивает
 * пропущенное через REST (этим занимаются репозитории по событию reconnected).
 */
@Singleton
class RealtimeClient @Inject constructor(
    @Named("wsOkHttp") okHttpClient: okhttp3.OkHttpClient,
    @Named("wsUrl") private val wsUrl: String,
    private val tokenStorage: TokenStorage,
    private val json: Json,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val stompClient = StompClient(okHttpClient)
    private var session: StompSession? = null
    private var connectionJob: Job? = null

    private val _messages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 128)
    /** Входящие сообщения чата (и echo своих) */
    val messages: SharedFlow<ChatMessage> = _messages

    private val _events = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 128)
    /** События: match.created, like.received, chat.typing, chat.read, presence.changed... */
    val events: SharedFlow<RealtimeEvent> = _events

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    /** Импульс после успешного (пере)подключения — сигнал репозиториям дотянуть пропущенное */
    private val _reconnected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val reconnected: SharedFlow<Unit> = _reconnected

    fun start() {
        if (connectionJob?.isActive == true) return
        connectionJob = scope.launch { connectionLoop() }
    }

    fun stop() {
        connectionJob?.cancel()
        connectionJob = null
        session?.close()
        session = null
        _connected.value = false
    }

    fun sendChatMessage(conversationId: String, content: String?, mediaKey: String?, clientTempId: String) {
        val payload = ChatSendPayloadDto(
            conversationId = conversationId,
            type = if (mediaKey != null) "IMAGE" else "TEXT",
            content = content,
            mediaKey = mediaKey,
            clientTempId = clientTempId,
        )
        session?.send("/app/chat.send", json.encodeToString(ChatSendPayloadDto.serializer(), payload))
    }

    fun sendTyping(conversationId: String, isTyping: Boolean) {
        val payload = TypingPayloadDto(conversationId, isTyping)
        session?.send("/app/chat.typing", json.encodeToString(TypingPayloadDto.serializer(), payload))
    }

    fun sendRead(conversationId: String) {
        session?.send("/app/chat.read", json.encodeToString(ReadPayloadDto.serializer(), ReadPayloadDto(conversationId)))
    }

    private suspend fun connectionLoop() {
        var backoffSeconds = 1L
        while (currentCoroutineContextIsActive()) {
            val token = tokenStorage.accessToken()
            if (token == null) {
                delay(3.seconds)
                continue
            }

            val current = stompClient.open(wsUrl, token)
            session = current

            var heartbeatJob: Job? = null
            try {
                current.events.collect { event ->
                    when (event) {
                        is StompEvent.Connected -> {
                            backoffSeconds = 1L
                            _connected.value = true
                            current.subscribe("/user/queue/messages")
                            current.subscribe("/user/queue/events")
                            current.subscribe("/user/queue/errors")
                            _reconnected.tryEmit(Unit)
                            // presence-маячок: сервер держит online с TTL 90с
                            heartbeatJob = scope.launch {
                                while (isActive) {
                                    delay(45.seconds)
                                    current.send("/app/presence.heartbeat", "{}")
                                }
                            }
                        }
                        is StompEvent.Message -> handleMessage(event)
                        is StompEvent.Error -> Unit // логировать нечем без утечки контента
                        is StompEvent.Closed -> throw ConnectionClosedException(event.reason)
                    }
                }
            } catch (e: ConnectionClosedException) {
                // ожидаемо: уходим на переподключение
            } finally {
                heartbeatJob?.cancel()
                current.close()
                _connected.value = false
            }

            delay(backoffSeconds.seconds)
            backoffSeconds = (backoffSeconds * 2).coerceAtMost(30)
        }
    }

    private fun handleMessage(event: StompEvent.Message) {
        val destination = event.frame.headers["destination"] ?: return
        when {
            destination.endsWith("/queue/messages") -> {
                runCatching { json.decodeFromString(MessageDto.serializer(), event.frame.body) }
                    .onSuccess { _messages.tryEmit(it.toDomain()) }
            }
            destination.endsWith("/queue/events") -> {
                runCatching { json.decodeFromString(RealtimeEventDto.serializer(), event.frame.body) }
                    .onSuccess { _events.tryEmit(it.toDomain()) }
            }
        }
    }

    private class ConnectionClosedException(reason: String) : Exception(reason)

    private suspend fun currentCoroutineContextIsActive(): Boolean =
        kotlinx.coroutines.currentCoroutineContext().isActive
}
