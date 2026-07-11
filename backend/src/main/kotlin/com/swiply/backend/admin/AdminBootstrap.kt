package com.swiply.backend.admin

import com.swiply.backend.auth.User
import com.swiply.backend.auth.UserRepository
import com.swiply.backend.common.UserRole
import com.swiply.backend.config.SwiplyProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
@Order(5)
class AdminBootstrap(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val props: SwiplyProperties,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        val email = props.admin.bootstrapEmail.trim().lowercase()
        if (userRepository.existsByEmail(email)) return
        userRepository.save(
            User(
                email = email,
                passwordHash = passwordEncoder.encode(props.admin.bootstrapPassword),
                role = UserRole.ADMIN,
                emailVerified = true,
            ),
        )
        log.info("Создан бутстрап-админ {} (пароль из ADMIN_PASSWORD)", email)
    }
}
