package com.swiply.backend.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.swiply.backend.config.userId
import com.swiply.backend.matching.MatchRepository
import com.swiply.backend.notification.RealtimeEvent
import com.swiply.backend.notification.RealtimeNotifier
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import java.util.UUID

private const val PRESENCE_CHANNEL = "presence-events"

data class PresenceChange(val userId: UUID, val online: Boolean)


@Component
class PresenceSessionListener(
    private val presenceService: PresenceService,
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onConnected(event: SessionConnectedEvent) {
        val userId = event.user?.userId() ?: return
        presenceService.markOnline(userId)
        publish(PresenceChange(userId, online = true))
        log.debug("WS connect: {} online", userId)
    }

    @EventListener
    fun onDisconnected(event: SessionDisconnectEvent) {
        val userId = event.user?.userId() ?: return
        presenceService.markOffline(userId)
        publish(PresenceChange(userId, online = false))
        log.debug("WS disconnect: {} offline", userId)
    }

    private fun publish(change: PresenceChange) {
        redis.convertAndSend(PRESENCE_CHANNEL, objectMapper.writeValueAsString(change))
    }
}


@Component
class PresenceBroadcastSubscriber(
    private val matchRepository: MatchRepository,
    private val realtimeNotifier: RealtimeNotifier,
    private val objectMapper: ObjectMapper,
) {

    fun onMessage(json: String) {
        val change: PresenceChange = objectMapper.readValue(json)
        val matches = matchRepository.findActiveFor(
            change.userId,
            null,
            org.springframework.data.domain.PageRequest.of(0, 200),
        )
        matches.forEach { match ->
            realtimeNotifier.sendEvent(
                match.otherUserId(change.userId),
                RealtimeEvent(
                    "presence.changed",
                    mapOf("userId" to change.userId.toString(), "online" to change.online),
                ),
            )
        }
    }
}

@Configuration
class PresenceRedisConfig {

    @Bean
    fun presenceListenerContainer(
        connectionFactory: RedisConnectionFactory,
        subscriber: PresenceBroadcastSubscriber,
    ): RedisMessageListenerContainer =
        RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(
                { message, _ -> subscriber.onMessage(String(message.body, Charsets.UTF_8)) },
                ChannelTopic(PRESENCE_CHANNEL),
            )
        }
}
