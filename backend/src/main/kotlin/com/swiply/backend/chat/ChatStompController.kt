package com.swiply.backend.chat

import com.swiply.backend.common.ApiException
import com.swiply.backend.config.userId
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.security.Principal


@Controller
class ChatStompController(
    private val chatService: ChatService,
    private val presenceService: PresenceService,
    private val messagingTemplate: SimpMessagingTemplate,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @MessageMapping("chat.send")
    fun send(@Payload payload: ChatSendPayload, principal: Principal) {
        handle(principal) { chatService.sendMessage(principal.userId(), payload) }
    }

    @MessageMapping("chat.typing")
    fun typing(@Payload payload: TypingPayload, principal: Principal) {
        handle(principal) { chatService.typing(principal.userId(), payload) }
    }

    @MessageMapping("chat.read")
    fun read(@Payload payload: ReadPayload, principal: Principal) {
        handle(principal) { chatService.markRead(principal.userId(), payload.conversationId) }
    }

    @MessageMapping("presence.heartbeat")
    fun heartbeat(principal: Principal) {
        presenceService.markOnline(principal.userId())
    }

    private fun handle(principal: Principal, block: () -> Unit) {
        try {
            block()
        } catch (e: ApiException) {
            messagingTemplate.convertAndSendToUser(
                principal.name,
                "/queue/errors",
                mapOf("code" to e.code, "message" to e.message),
            )
        } catch (e: Exception) {
            log.error("WS-ошибка у {}", principal.name, e)
            messagingTemplate.convertAndSendToUser(
                principal.name,
                "/queue/errors",
                mapOf("code" to "INTERNAL_ERROR", "message" to "Внутренняя ошибка"),
            )
        }
    }
}
