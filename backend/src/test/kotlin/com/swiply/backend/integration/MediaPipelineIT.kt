package com.swiply.backend.integration

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Полный медиапайплайн: multipart → magic bytes → переэнкод (EXIF-стрип) →
 * MinIO → RabbitMQ-воркер (thumbnail + авто-модерация) → presigned URL.
 */
class MediaPipelineIT : IntegrationTestBase() {

    private fun pngBytes(width: Int = 900, height: Int = 700): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().apply {
            color = java.awt.Color(255, 84, 120)
            fillRect(0, 0, width, height)
            dispose()
        }
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        return out.toByteArray()
    }

    private fun uploadPhoto(token: String, bytes: ByteArray, filename: String = "photo.png") =
        rest.exchange(
            "/api/v1/users/me/photos",
            HttpMethod.POST,
            HttpEntity(
                LinkedMultiValueMap<String, Any>().apply {
                    add(
                        "file",
                        object : ByteArrayResource(bytes) {
                            override fun getFilename(): String = filename
                        },
                    )
                },
                HttpHeaders().apply {
                    contentType = MediaType.MULTIPART_FORM_DATA
                    setBearerAuth(token)
                },
            ),
            String::class.java,
        )

    @Test
    fun `фото проходит весь пайплайн до APPROVED с thumbnail`() {
        val user = registerUser(displayName = "Фотограф")

        val response = uploadPhoto(user.accessToken, pngBytes())
        assertEquals(201, response.statusCode.value(), "тело: ${response.body}")
        val uploaded = json(response)
        assertEquals("PENDING", uploaded["status"].asText())
        assertTrue(uploaded["url"].asText().isNotBlank(), "presigned URL выдан сразу")

        // Воркер асинхронно генерирует thumbnail'ы и проставляет APPROVED
        await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted {
            val me = json(get("/api/v1/users/me", user.accessToken))
            val photo = me["photos"].first()
            assertEquals("APPROVED", photo["status"].asText())
            assertTrue(photo["thumbUrl"].asText().isNotBlank())
        }
    }

    @Test
    fun `не-изображение отклоняется по magic bytes`() {
        val user = registerUser()
        val response = uploadPhoto(user.accessToken, "именины у psd-файла".toByteArray(), "hack.jpg")
        assertEquals(400, response.statusCode.value())
        assertEquals("UNSUPPORTED_IMAGE", json(response)["code"].asText())
    }

    @Test
    fun `удаление фото уплотняет позиции`() {
        val user = registerUser()
        uploadPhoto(user.accessToken, pngBytes(300, 300))
        uploadPhoto(user.accessToken, pngBytes(310, 310))

        val me = json(get("/api/v1/users/me", user.accessToken))
        val firstId = me["photos"][0]["id"].asText()
        assertEquals(2, me["photos"].size())

        delete("/api/v1/users/me/photos/$firstId", token = user.accessToken)

        val after = json(get("/api/v1/users/me", user.accessToken))
        assertEquals(1, after["photos"].size())
        assertEquals(0, after["photos"][0]["position"].asInt(), "позиции уплотнены")
    }

    @Test
    fun `порядок фото меняется целиком и валидируется`() {
        val user = registerUser()
        uploadPhoto(user.accessToken, pngBytes(300, 300))
        uploadPhoto(user.accessToken, pngBytes(310, 310))

        val me = json(get("/api/v1/users/me", user.accessToken))
        val ids = me["photos"].map { it["id"].asText() }

        val reordered = json(
            put("/api/v1/users/me/photos/order", mapOf("photoIds" to ids.reversed()), user.accessToken),
        )
        assertEquals(ids.reversed(), reordered.map { it["id"].asText() })

        // неполный список отклоняется
        val bad = put("/api/v1/users/me/photos/order", mapOf("photoIds" to listOf(ids.first())), user.accessToken)
        assertEquals(400, bad.statusCode.value())
    }
}
