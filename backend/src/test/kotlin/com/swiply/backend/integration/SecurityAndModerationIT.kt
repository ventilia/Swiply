package com.swiply.backend.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SecurityAndModerationIT : IntegrationTestBase() {

    private fun adminToken(): String {
        val response = post(
            "/api/v1/auth/login",
            mapOf("email" to "admin@swiply.local", "password" to "admin12345"),
        )
        check(response.statusCode.is2xxSuccessful) { "Бутстрап-админ не залогинился: ${response.body}" }
        return json(response)["accessToken"].asText()
    }

    @Test
    fun `защищённые эндпоинты без токена - 401, публичные открыты`() {
        assertEquals(401, get("/api/v1/users/me").statusCode.value())
        assertEquals(401, get("/api/v1/matches").statusCode.value())
        assertEquals(401, post("/api/v1/swipes", mapOf("x" to 1)).statusCode.value())

        val health = get("/actuator/health")
        assertEquals(200, health.statusCode.value())
        assertTrue(health.body!!.contains("UP"))

        assertEquals(200, get("/v3/api-docs").statusCode.value())
    }

    @Test
    fun `админ-API запрещён обычному пользователю - 403`() {
        val user = registerUser()
        assertEquals(403, get("/api/v1/admin/stats", user.accessToken).statusCode.value())
        assertEquals(403, get("/api/v1/admin/users", user.accessToken).statusCode.value())
        assertEquals(
            403,
            post("/api/v1/admin/users/${user.id}/ban", mapOf("reason" to "x"), user.accessToken).statusCode.value(),
        )
    }

    @Test
    fun `репорт уходит в очередь, бан отключает доступ мгновенно, unban возвращает`() {
        val admin = adminToken()
        val reporter = registerUser(displayName = "Сознательный")
        val villain = registerUser(displayName = "Нарушитель")

        // жалоба
        val report = post(
            "/api/v1/reports",
            mapOf("targetUserId" to villain.id.toString(), "reason" to "SPAM", "description" to "рассылает рекламу"),
            reporter.accessToken,
        )
        assertEquals(201, report.statusCode.value())
        val reportId = json(report)["reportId"].asText()

        // очередь модерации видит репорт
        val queue = json(get("/api/v1/admin/reports?status=PENDING", admin))
        assertTrue(queue.any { it["id"].asText() == reportId })

        // бан: старый access-токен гаснет сразу (Redis-блок), логин запрещён
        post("/api/v1/admin/users/${villain.id}/ban", mapOf("reason" to "спам"), admin)
        assertEquals(401, get("/api/v1/users/me", villain.accessToken).statusCode.value())
        val bannedLogin = post(
            "/api/v1/auth/login",
            mapOf("email" to villain.email, "password" to villain.password),
        )
        assertEquals(403, bannedLogin.statusCode.value())
        assertEquals("ACCOUNT_BANNED", json(bannedLogin)["code"].asText())

        // refresh забаненного тоже мёртв (все отозваны)
        assertEquals(
            401,
            post("/api/v1/auth/refresh", mapOf("refreshToken" to villain.refreshToken)).statusCode.value(),
        )

        // закрытие репорта
        post("/api/v1/admin/reports/$reportId/resolve", mapOf("dismiss" to false), admin)
        val resolved = json(get("/api/v1/admin/reports?status=ACTIONED", admin))
        assertTrue(resolved.any { it["id"].asText() == reportId })

        // аудит-лог содержит бан
        val history = json(get("/api/v1/admin/users/${villain.id}/moderation-history", admin))
        assertTrue(history.any { it["action"].asText() == "BAN" })

        // unban возвращает доступ
        post("/api/v1/admin/users/${villain.id}/unban", null, admin)
        val loginBack = post(
            "/api/v1/auth/login",
            mapOf("email" to villain.email, "password" to villain.password),
        )
        assertEquals(200, loginBack.statusCode.value())
    }

    @Test
    fun `удаление аккаунта анонимизирует и рвёт сессии`() {
        val user = registerUser(displayName = "Уходящий")

        // неверный пароль — отказ
        assertEquals(
            401,
            delete("/api/v1/users/me", mapOf("password" to "не тот пароль"), user.accessToken).statusCode.value(),
        )

        val deleted = delete("/api/v1/users/me", mapOf("password" to user.password), user.accessToken)
        assertEquals(200, deleted.statusCode.value())

        // токен мёртв, логин невозможен
        assertEquals(401, get("/api/v1/users/me", user.accessToken).statusCode.value())
        assertEquals(
            401,
            post("/api/v1/auth/login", mapOf("email" to user.email, "password" to user.password)).statusCode.value(),
        )
    }

    @Test
    fun `блокировка скрывает из ленты в обе стороны`() {
        val a = registerUser(displayName = "Блок-А", gender = "FEMALE", interestedIn = listOf("MALE"))
        val b = registerUser(displayName = "Блок-Б", gender = "MALE", interestedIn = listOf("FEMALE"))
        setLocation(a, 55.75, 37.62)
        setLocation(b, 55.751, 37.621)

        assertTrue(json(get("/api/v1/discovery/candidates", a.accessToken))["items"].any { it["userId"].asText() == b.id.toString() })

        post("/api/v1/users/${b.id}/block", null, a.accessToken)

        assertTrue(json(get("/api/v1/discovery/candidates", a.accessToken))["items"].none { it["userId"].asText() == b.id.toString() })
        assertTrue(json(get("/api/v1/discovery/candidates", b.accessToken))["items"].none { it["userId"].asText() == a.id.toString() })

        // профиль заблокированного недоступен
        assertEquals(404, get("/api/v1/users/${b.id}", a.accessToken).statusCode.value())
        assertEquals(404, get("/api/v1/users/${a.id}", b.accessToken).statusCode.value())

        val blockedList = json(get("/api/v1/users/me/blocked", a.accessToken))
        assertNotNull(blockedList.firstOrNull { it["userId"].asText() == b.id.toString() })
    }
}
