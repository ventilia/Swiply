package com.swiply.backend.notification

import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.util.UUID


data class RealtimeEvent(
    val type: String,
    val payload: Map<String, Any?> = emptyMap(),
)

@Component
class RealtimeNotifier(private val messagingTemplate: SimpMessagingTemplate) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sendEvent(userId: UUID, event: RealtimeEvent) {
        try {
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/events", event)
        } catch (e: Exception) {

            log.warn("Не удалось отправить WS-событие {} пользователю {}: {}", event.type, userId, e.message)
        }
    }
}
