package com.swiply.backend.notification

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID


fun interface PushNotificationSender {
    fun send(userId: UUID, type: NotificationType, title: String, body: String)
}

@Configuration
class PushConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnMissingBean(PushNotificationSender::class)
    fun loggingPushSender(): PushNotificationSender =
        PushNotificationSender { userId, type, _, _ ->
            log.info("[PUSH:no-op] user={} type={}", userId, type)
        }
}
