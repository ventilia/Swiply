package com.swiply.backend.common

import com.swiply.backend.auth.AuthPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

object SecurityUtils {

    fun currentPrincipal(): AuthPrincipal {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException()
        return auth.principal as? AuthPrincipal ?: throw UnauthorizedException()
    }

    fun currentUserId(): UUID = currentPrincipal().userId
}
