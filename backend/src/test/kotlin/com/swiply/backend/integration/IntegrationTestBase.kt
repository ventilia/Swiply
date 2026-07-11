package com.swiply.backend.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName
import java.util.UUID

/**
 * База интеграционных тестов: один общий набор контейнеров на весь прогон
 * (стартуют лениво при первом обращении, живут до конца JVM).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTestBase {

    @Autowired
    protected lateinit var rest: TestRestTemplate

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @LocalServerPort
    protected var port: Int = 0

    companion object {
        private val postgres = PostgreSQLContainer(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"),
        ).apply { start() }

        private val mongo = MongoDBContainer(DockerImageName.parse("mongo:7")).apply { start() }

        private val redis = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .apply { start() }

        private val rabbit = RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"))
            .apply { start() }

        private val minio = MinIOContainer(DockerImageName.parse("minio/minio:latest")).apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun registerProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl)
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
            registry.add("spring.rabbitmq.host", rabbit::getHost)
            registry.add("spring.rabbitmq.port", rabbit::getAmqpPort)
            registry.add("spring.rabbitmq.username", rabbit::getAdminUsername)
            registry.add("spring.rabbitmq.password", rabbit::getAdminPassword)
            registry.add("swiply.media.endpoint", minio::getS3URL)
            registry.add("swiply.media.public-endpoint", minio::getS3URL)
            registry.add("swiply.media.access-key", minio::getUserName)
            registry.add("swiply.media.secret-key", minio::getPassword)
            registry.add("logging.level.org.springframework.security") { "DEBUG" }
            // тесты не должны душить друг друга общими лимитами
            registry.add("swiply.limits.register-per-hour") { 100000 }
            registry.add("swiply.limits.login-per-minute") { 100000 }
            registry.add("swiply.limits.swipes-per-day") { 100000 }
            registry.add("swiply.limits.superlikes-per-day") { 100000 }
            registry.add("swiply.limits.messages-per-10s") { 100000 }
            registry.add("swiply.limits.reports-per-day") { 100000 }
        }
    }

    // ===== HTTP-хелперы =====

    protected fun get(path: String, token: String? = null): ResponseEntity<String> =
        rest.exchange(path, HttpMethod.GET, HttpEntity<Void>(headers(token)), String::class.java)

    protected fun post(path: String, body: Any? = null, token: String? = null): ResponseEntity<String> =
        rest.exchange(path, HttpMethod.POST, HttpEntity(body, headers(token)), String::class.java)

    protected fun put(path: String, body: Any?, token: String? = null): ResponseEntity<String> =
        rest.exchange(path, HttpMethod.PUT, HttpEntity(body, headers(token)), String::class.java)

    protected fun delete(path: String, body: Any? = null, token: String? = null): ResponseEntity<String> =
        rest.exchange(path, HttpMethod.DELETE, HttpEntity(body, headers(token)), String::class.java)

    protected fun json(response: ResponseEntity<String>): JsonNode =
        objectMapper.readTree(response.body ?: "{}")

    private fun headers(token: String?): HttpHeaders = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
        token?.let { setBearerAuth(it) }
    }

    // ===== Доменные хелперы =====

    data class TestUser(
        val id: UUID,
        val email: String,
        val password: String,
        val accessToken: String,
        val refreshToken: String,
    )

    protected fun registerUser(
        displayName: String = "Тест",
        gender: String = "MALE",
        interestedIn: List<String> = listOf("FEMALE"),
        birthDate: String = "1995-06-15",
    ): TestUser {
        val email = "user-${UUID.randomUUID()}@test.swiply"
        val password = "password123"
        val response = post(
            "/api/v1/auth/register",
            mapOf(
                "email" to email,
                "password" to password,
                "displayName" to displayName,
                "birthDate" to birthDate,
                "gender" to gender,
                "interestedIn" to interestedIn,
            ),
        )
        check(response.statusCode.value() == 201) { "Регистрация не удалась: ${response.body}" }
        val node = json(response)
        return TestUser(
            id = UUID.fromString(node["user"]["id"].asText()),
            email = email,
            password = password,
            accessToken = node["accessToken"].asText(),
            refreshToken = node["refreshToken"].asText(),
        )
    }

    protected fun setLocation(user: TestUser, lat: Double, lon: Double, city: String? = null) {
        val response = put(
            "/api/v1/users/me/location",
            mapOf("latitude" to lat, "longitude" to lon, "city" to city),
            user.accessToken,
        )
        check(response.statusCode.is2xxSuccessful) { "Локация не обновилась: ${response.body}" }
    }

    protected fun swipe(user: TestUser, toUserId: UUID, action: String = "LIKE"): JsonNode {
        val response = post(
            "/api/v1/swipes",
            mapOf("toUserId" to toUserId.toString(), "action" to action),
            user.accessToken,
        )
        check(response.statusCode.is2xxSuccessful) { "Свайп не прошёл: ${response.body}" }
        return json(response)
    }
}
