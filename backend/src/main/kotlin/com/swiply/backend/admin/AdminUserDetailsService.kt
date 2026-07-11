package com.swiply.backend.admin

import com.swiply.backend.auth.UserRepository
import com.swiply.backend.common.UserRole
import com.swiply.backend.common.UserStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.security.core.userdetails.User as SpringUser


@Service
class AdminUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username.trim().lowercase())
            ?: throw UsernameNotFoundException("Пользователь не найден")
        if (user.status != UserStatus.ACTIVE || user.role == UserRole.USER) {
            throw UsernameNotFoundException("Нет доступа к админке")
        }
        return SpringUser(
            user.email,
            user.passwordHash,
            listOf(SimpleGrantedAuthority("ROLE_${user.role.name}")),
        )
    }
}
