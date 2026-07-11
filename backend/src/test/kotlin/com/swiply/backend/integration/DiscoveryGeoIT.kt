package com.swiply.backend.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Гео-поиск: два аккаунта рядом видят друг друга, дальний — нет.
 * Москва (центр) vs Санкт-Петербург (~635 км).
 */
class DiscoveryGeoIT : IntegrationTestBase() {

    @Test
    fun `кандидаты подбираются по гео и взаимным фильтрам`() {
        val anna = registerUser(displayName = "Анна-гео", gender = "FEMALE", interestedIn = listOf("MALE"))
        val boris = registerUser(displayName = "Борис-гео", gender = "MALE", interestedIn = listOf("FEMALE"))
        val piter = registerUser(displayName = "Пётр-гео", gender = "MALE", interestedIn = listOf("FEMALE"))

        // без локации лента недоступна с понятной ошибкой
        val noLocation = get("/api/v1/discovery/candidates", anna.accessToken)
        assertEquals(400, noLocation.statusCode.value())
        assertEquals("NO_LOCATION", json(noLocation)["code"].asText())

        setLocation(anna, 55.7558, 37.6176, "Москва")
        setLocation(boris, 55.7600, 37.6200, "Москва")
        setLocation(piter, 59.9311, 30.3609, "Санкт-Петербург")

        val annaFeed = json(get("/api/v1/discovery/candidates", anna.accessToken))
        val annaIds = annaFeed["items"].map { it["userId"].asText() }
        assertTrue(boris.id.toString() in annaIds, "Борис в ~0.5 км должен быть в ленте")
        assertTrue(piter.id.toString() !in annaIds, "Питер в 600+ км не должен попасть в ленту (лимит 50 км)")
        assertTrue(anna.id.toString() !in annaIds, "сам себе не кандидат")

        val borisCard = annaFeed["items"].first { it["userId"].asText() == boris.id.toString() }
        assertEquals("Борис-гео", borisCard["displayName"].asText())
        assertTrue(borisCard["distanceKm"].asInt() >= 1, "дистанция округлённая, минимум 1 км")

        // взаимность ленты
        val borisFeed = json(get("/api/v1/discovery/candidates", boris.accessToken))
        assertTrue(borisFeed["items"].any { it["userId"].asText() == anna.id.toString() })

        // свайпнутый кандидат исчезает из ленты
        swipe(anna, boris.id, action = "DISLIKE")
        val afterSwipe = json(get("/api/v1/discovery/candidates", anna.accessToken))
        assertTrue(afterSwipe["items"].none { it["userId"].asText() == boris.id.toString() })
    }

    @Test
    fun `фильтр по полу и interested_in работает в обе стороны`() {
        // Дана ищет только женщин — мужчина её ленты не увидит и наоборот
        val dana = registerUser(displayName = "Дана", gender = "FEMALE", interestedIn = listOf("FEMALE"))
        val egor = registerUser(displayName = "Егор", gender = "MALE", interestedIn = listOf("FEMALE"))
        setLocation(dana, 55.70, 37.60)
        setLocation(egor, 55.70, 37.61)

        val danaFeed = json(get("/api/v1/discovery/candidates", dana.accessToken))
        assertTrue(danaFeed["items"].none { it["userId"].asText() == egor.id.toString() })

        val egorFeed = json(get("/api/v1/discovery/candidates", egor.accessToken))
        assertTrue(egorFeed["items"].none { it["userId"].asText() == dana.id.toString() })
    }
}
