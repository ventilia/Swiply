package com.swiply.backend.integration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthFlowIT : IntegrationTestBase() {

    @Test
    fun `полный цикл - регистрация, me, refresh с ротацией, logout`() {
        val user = registerUser(displayName = "Циклоп")

        // me с access-токеном
        val me = json(get("/api/v1/users/me", user.accessToken))
        assertEquals(user.email, me["email"].asText())
        assertEquals("Циклоп", me["displayName"].asText())
        assertEquals(18, me["minAgePref"].asInt())

        // refresh: выдаёт новую пару и отзывает старый refresh
        val refreshed = json(post("/api/v1/auth/refresh", mapOf("refreshToken" to user.refreshToken)))
        val newAccess = refreshed["accessToken"].asText()
        val newRefresh = refreshed["refreshToken"].asText()
        assertNotEquals(user.refreshToken, newRefresh)

        // старый refresh больше не работает (ротация)
        val reuse = post("/api/v1/auth/refresh", mapOf("refreshToken" to user.refreshToken))
        assertEquals(401, reuse.statusCode.value())

        // новый access валиден
        assertEquals(200, get("/api/v1/users/me", newAccess).statusCode.value())

        // logout отзывает refresh
        post("/api/v1/auth/logout", mapOf("refreshToken" to newRefresh), newAccess)
        assertEquals(401, post("/api/v1/auth/refresh", mapOf("refreshToken" to newRefresh)).statusCode.value())
    }

    @Test
    fun `логин с неверным паролем - 401 без деталей`() {
        val user = registerUser()
        val response = post("/api/v1/auth/login", mapOf("email" to user.email, "password" to "wrong-password"))
        assertEquals(401, response.statusCode.value())
    }

    @Test
    fun `регистрация до 18 лет жёстко запрещена`() {
        val response = post(
            "/api/v1/auth/register",
            mapOf(
                "email" to "teen-${System.nanoTime()}@test.swiply",
                "password" to "password123",
                "displayName" to "Юнец",
                "birthDate" to java.time.LocalDate.now().minusYears(17).toString(),
                "gender" to "MALE",
                "interestedIn" to listOf("FEMALE"),
            ),
        )
        assertEquals(400, response.statusCode.value())
        assertEquals("UNDERAGE", json(response)["code"].asText())
    }

    @Test
    fun `повторная регистрация на ту же почту - 409`() {
        val user = registerUser()
        val response = post(
            "/api/v1/auth/register",
            mapOf(
                "email" to user.email,
                "password" to "password123",
                "displayName" to "Дубль",
                "birthDate" to "1990-01-01",
                "gender" to "MALE",
                "interestedIn" to listOf("FEMALE"),
            ),
        )
        assertEquals(409, response.statusCode.value())
    }

    @Test
    fun `валидация - кривой email и короткий пароль`() {
        val response = post(
            "/api/v1/auth/register",
            mapOf(
                "email" to "не-почта",
                "password" to "123",
                "displayName" to "X",
                "birthDate" to "1990-01-01",
                "gender" to "MALE",
                "interestedIn" to listOf("FEMALE"),
            ),
        )
        assertEquals(400, response.statusCode.value())
        val body = json(response)
        assertEquals("VALIDATION_ERROR", body["code"].asText())
        assertTrue(body["fieldErrors"].has("email"))
        assertTrue(body["fieldErrors"].has("password"))
    }
}
