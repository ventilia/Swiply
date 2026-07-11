package com.swiply.backend.config

import com.swiply.backend.auth.JwtService
import com.swiply.backend.auth.TokenBlacklistService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal
import java.util.UUID


data class StompPrincipal(private val userId: String) : Principal {
    override fun getName(): String = userId
}

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val jwtService: JwtService,
    private val blacklist: TokenBlacklistService,
) : WebSocketMessageBrokerConfigurer {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/queue", "/topic")
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .addInterceptors(jwtHandshakeInterceptor())
            .setHandshakeHandler(object : DefaultHandshakeHandler() {
                override fun determineUser(
                    request: ServerHttpRequest,
                    wsHandler: WebSocketHandler,
                    attributes: MutableMap<String, Any>,
                ): Principal? = attributes["principal"] as? StompPrincipal
            })
    }

    private fun jwtHandshakeInterceptor() = object : HandshakeInterceptor {
        override fun beforeHandshake(
            request: ServerHttpRequest,
            response: ServerHttpResponse,
            wsHandler: WebSocketHandler,
            attributes: MutableMap<String, Any>,
        ): Boolean {
            val token = extractToken(request) ?: run {
                log.debug("WS handshake отклонён: нет токена")
                return false
            }
            return try {
                val principal = jwtService.parse(token)
                if (blacklist.isJtiBlacklisted(principal.jti) || blacklist.isUserBlocked(principal.userId)) {
                    return false
                }
                attributes["principal"] = StompPrincipal(principal.userId.toString())
                attributes["userId"] = principal.userId
                true
            } catch (e: Exception) {
                log.debug("WS handshake отклонён: {}", e.message)
                false
            }
        }

        override fun afterHandshake(
            request: ServerHttpRequest,
            response: ServerHttpResponse,
            wsHandler: WebSocketHandler,
            exception: Exception?,
        ) = Unit
    }

    private fun extractToken(request: ServerHttpRequest): String? {
        val header = request.headers.getFirst("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            return header.removePrefix("Bearer ").trim()
        }
        val servletRequest = (request as? ServletServerHttpRequest)?.servletRequest
        return servletRequest?.getParameter("token")
    }
}


fun Principal.userId(): UUID = UUID.fromString(name)
