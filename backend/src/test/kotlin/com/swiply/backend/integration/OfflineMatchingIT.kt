package com.swiply.backend.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * ОБЯЗАТЕЛЬНЫЙ сценарий из ТЗ: мэтчинг для офлайн-получателя.
 *
 * Борис лайкнул Анну и «ушёл в офлайн» (ни одного WebSocket-подключения).
 * Анна лайкает Бориса — мэтч обязан создаться немедленно на сервере,
 * а Борис должен увидеть его при следующем открытии приложения через
 * персистентный inbox и список мэтчей.
 */
class OfflineMatchingIT : IntegrationTestBase() {

    @Test
    fun `мэтч создаётся сразу, даже если второй пользователь офлайн`() {
        val anna = registerUser(displayName = "Анна", gender = "FEMALE", interestedIn = listOf("MALE"))
        val boris = registerUser(displayName = "Борис", gender = "MALE", interestedIn = listOf("FEMALE"))

        // Борис лайкает Анну — взаимности ещё нет
        val first = swipe(boris, anna.id)
        assertFalse(first["matched"].asBoolean())

        // у Анны появилось NEW_LIKE в inbox
        val annaLikes = json(get("/api/v1/notifications?unread=true", anna.accessToken))
        assertTrue(annaLikes["items"].any { it["type"].asText() == "NEW_LIKE" })

        // Анна отвечает взаимностью; Борис офлайн — WebSocket не открывал
        val second = swipe(anna, boris.id)
        assertTrue(second["matched"].asBoolean(), "мэтч должен создаться синхронно")
        val matchId = second["matchId"].asText()
        assertNotNull(matchId)

        // Борис «возвращается» и видит мэтч в inbox уведомлений...
        val borisInbox = json(get("/api/v1/notifications?unread=true", boris.accessToken))
        val matchNotification = borisInbox["items"].firstOrNull { it["type"].asText() == "NEW_MATCH" }
        assertNotNull(matchNotification, "офлайн-пользователь обязан получить NEW_MATCH в inbox")
        assertEquals(matchId, matchNotification["payload"]["matchId"].asText())

        // ...и в списке мэтчей
        val borisMatches = json(get("/api/v1/matches", boris.accessToken))
        assertTrue(borisMatches["items"].any { it["matchId"].asText() == matchId })
        val annaMatches = json(get("/api/v1/matches", anna.accessToken))
        assertTrue(annaMatches["items"].any { it["matchId"].asText() == matchId })

        // повторный свайп идемпотентен и возвращает тот же мэтч
        val repeat = swipe(anna, boris.id)
        assertTrue(repeat["matched"].asBoolean())
        assertEquals(matchId, repeat["matchId"].asText())

        // счётчик непрочитанного у Бориса ненулевой
        val unread = json(get("/api/v1/notifications/unread-count", boris.accessToken))
        assertTrue(unread["unread"].asLong() >= 1)
    }

    @Test
    fun `дизлайк не создаёт мэтч при встречном лайке`() {
        val a = registerUser(displayName = "Вера", gender = "FEMALE", interestedIn = listOf("MALE"))
        val b = registerUser(displayName = "Глеб", gender = "MALE", interestedIn = listOf("FEMALE"))

        swipe(b, a.id)
        val result = swipe(a, b.id, action = "DISLIKE")
        assertFalse(result["matched"].asBoolean())

        val matches = json(get("/api/v1/matches", b.accessToken))
        assertTrue(matches["items"].none { it["userId"].asText() == a.id.toString() })
    }
}
