package com.swiply.backend.auth

import com.swiply.backend.common.UnauthorizedException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val blacklist: TokenBlacklistService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            try {
                val principal = jwtService.parse(token)
                val active = !blacklist.isJtiBlacklisted(principal.jti) &&
                    !blacklist.isUserBlocked(principal.userId)
                if (active) {
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${principal.role.name}"))
                    val authentication = UsernamePasswordAuthenticationToken(principal, null, authorities)

                    val context = SecurityContextHolder.createEmptyContext()
                    context.authentication = authentication
                    SecurityContextHolder.setContext(context)
                }
            } catch (e: UnauthorizedException) {

                logger.debug("JWT отклонён: ${e.message}")
            }
        }
        filterChain.doFilter(request, response)
    }
}
