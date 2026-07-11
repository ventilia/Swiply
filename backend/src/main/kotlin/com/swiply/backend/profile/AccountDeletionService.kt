package com.swiply.backend.profile

import com.swiply.backend.auth.RefreshTokenRepository
import com.swiply.backend.auth.TokenBlacklistService
import com.swiply.backend.auth.UserRepository
import com.swiply.backend.common.UnauthorizedException
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.common.UserStatus
import com.swiply.backend.config.SwiplyProperties
import com.swiply.backend.discovery.DiscoveryService
import com.swiply.backend.media.PhotoService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID


@Service
class AccountDeletionService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val photoService: PhotoService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val blacklist: TokenBlacklistService,
    private val passwordEncoder: PasswordEncoder,
    private val discoveryService: DiscoveryService,
    private val props: SwiplyProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun deleteAccount(userId: UUID, password: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("USER_NOT_FOUND", "Пользователь не найден") }
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw UnauthorizedException("Неверный пароль")
        }

        photoService.deleteAllFor(userId)

        profileRepository.findById(userId).ifPresent { profile ->
            profile.displayName = "Удалённый пользователь"
            profile.bio = null
            profile.location = null
            profile.city = null
            profile.isDiscoverable = false
            profileRepository.save(profile)
        }

        user.status = UserStatus.DELETED
        user.email = "deleted-$userId@deleted.swiply.local"
        user.phone = null
        user.passwordHash = passwordEncoder.encode(UUID.randomUUID().toString())
        userRepository.save(user)

        refreshTokenRepository.revokeAllForUser(userId)
        blacklist.markUserBlocked(userId, props.jwt.accessTtl)
        discoveryService.invalidate(userId)
        log.info("Аккаунт {} удалён (анонимизирован)", userId)
    }
}
