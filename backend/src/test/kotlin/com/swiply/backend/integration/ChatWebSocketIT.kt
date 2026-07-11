package com.swiply.backend.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.util.UUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Realtime-чат через STOMP/WebSocket: JWT-handshake, доставка сообщений,
 * echo отправителю, история в MongoDB, read receipts.
 */
class ChatWebSocketIT : IntegrationTestBase() {

    private fun stompClient(): WebSocketStompClient {
        val mapper = ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()
        return WebSocketStompClient(StandardWebSocketClient()).apply {
            messageConverter = MappingJackson2MessageConverter().also { it.objectMapper = mapper }
        }
    }

    private fun connect(token: String): StompSession =
        stompClient()
            .connectAsync("ws://localhost:$port/ws?token=$token", object : StompSessionHandlerAdapter() {})
            .get(10, TimeUnit.SECONDS)

    private fun subscribeQueue(session: StompSession, destination: String): BlockingQueue<Map<String, Any?>> {
        val queue = LinkedBlockingQueue<Map<String, Any?>>()
        session.subscribe(
            destination,
            object : StompSessionHandlerAdapter() {
                override fun getPayloadType(headers: StompHeaders): Type = Map::class.java

                @Suppress("UNCHECKED_CAST")
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    queue.offer(payload as Map<String, Any?>)
                }
            },
        )
        return queue
    }

    private fun matchedPair(): Triple<TestUser, TestUser, String> {
        val a = registerUser(displayName = "Чат-Анна", gender = "FEMALE", interestedIn = listOf("MALE"))
        val b = registerUser(displayName = "Чат-Борис", gender = "MALE", interestedIn = listOf("FEMALE"))
        swipe(b, a.id)
        val match = swipe(a, b.id)
        assertTrue(match["matched"].asBoolean())
        val conversation = json(post("/api/v1/conversations/by-match/${match["matchId"].asText()}", null, a.accessToken))
        return Triple(a, b, conversation["id"].asText())
    }

    @Test
    fun `сообщение доставляется в реальном времени и сохраняется в истории`() {
        val (anna, boris, conversationId) = matchedPair()

        val annaSession = connect(anna.accessToken)
        val borisSession = connect(boris.accessToken)
        val borisInbox = subscribeQueue(borisSession, "/user/queue/messages")
        val annaEcho = subscribeQueue(annaSession, "/user/queue/messages")
        Thread.sleep(500) // подписки должны успеть зарегистрироваться на брокере

        annaSession.send(
            "/app/chat.send",
            mapOf(
                "conversationId" to conversationId,
                "type" to "TEXT",
                "content" to "Привет из теста!",
                "clientTempId" to "tmp-1",
            ),
        )

        val received = borisInbox.poll(10, TimeUnit.SECONDS)
        assertNotNull(received, "Борис должен получить сообщение по WebSocket")
        assertEquals("Привет из теста!", received["content"])
        assertEquals(conversationId, received["conversationId"])
        assertEquals("DELIVERED", received["status"], "получатель онлайн — статус DELIVERED")

        val echo = annaEcho.poll(10, TimeUnit.SECONDS)
        assertNotNull(echo, "отправитель получает echo для optimistic UI")
        assertEquals("tmp-1", echo["clientTempId"])

        // история сохранена в MongoDB и доступна по REST
        val history = json(get("/api/v1/conversations/$conversationId/messages", boris.accessToken))
        assertTrue(history["items"].any { it["content"].asText() == "Привет из теста!" })

        // превью и unread в списке диалогов Бориса
        val conversations = json(get("/api/v1/conversations", boris.accessToken))
        val dialog = conversations.first { it["id"].asText() == conversationId }
        assertEquals("Привет из теста!", dialog["lastMessage"]["preview"].asText())
        assertEquals(1, dialog["unreadCount"].asInt())

        annaSession.disconnect()
        borisSession.disconnect()
    }

    @Test
    fun `read receipt доходит до отправителя, unread обнуляется`() {
        val (anna, boris, conversationId) = matchedPair()

        val annaSession = connect(anna.accessToken)
        val annaEvents = subscribeQueue(annaSession, "/user/queue/events")
        Thread.sleep(500)

        // Анна пишет; Борис читает по REST (эквивалент chat.read)
        annaSession.send(
            "/app/chat.send",
            mapOf("conversationId" to conversationId, "type" to "TEXT", "content" to "Прочитай меня"),
        )
        Thread.sleep(700)
        post("/api/v1/conversations/$conversationId/read", null, boris.accessToken)

        // Анне прилетает событие chat.read
        var readEvent: Map<String, Any?>? = null
        val deadline = System.currentTimeMillis() + 10_000
        while (System.currentTimeMillis() < deadline) {
            val event = annaEvents.poll(1, TimeUnit.SECONDS) ?: continue
            if (event["type"] == "chat.read") {
                readEvent = event
                break
            }
        }
        assertNotNull(readEvent, "отправитель должен получить chat.read")

        val conversations = json(get("/api/v1/conversations", boris.accessToken))
        assertEquals(0, conversations.first { it["id"].asText() == conversationId }["unreadCount"].asInt())

        // статус сообщения в истории — READ
        val history = json(get("/api/v1/conversations/$conversationId/messages", anna.accessToken))
        assertEquals("READ", history["items"].first { it["content"].asText() == "Прочитай меня" }["status"].asText())

        annaSession.disconnect()
    }

    @Test
    fun `handshake без токена отклоняется`() {
        val failed = runCatching {
            stompClient()
                .connectAsync("ws://localhost:$port/ws", object : StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS)
        }
        assertTrue(failed.isFailure, "подключение без JWT должно быть отклонено до апгрейда")
    }

    @Test
    fun `чужой не может писать в диалог`() {
        val (_, _, conversationId) = matchedPair()
        val intruder = registerUser(displayName = "Чужак")

        val history = get("/api/v1/conversations/$conversationId/messages", intruder.accessToken)
        assertEquals(404, history.statusCode.value(), "участник ≠ читатель: диалог не раскрывается")
    }
}
