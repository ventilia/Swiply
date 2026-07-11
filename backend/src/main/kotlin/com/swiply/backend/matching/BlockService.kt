package com.swiply.backend.matching

import com.swiply.backend.auth.UserRepository
import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.discovery.DiscoveryService
import com.swiply.backend.notification.RealtimeEvent
import com.swiply.backend.notification.RealtimeNotifier
import com.swiply.backend.profile.ProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID


@Service
class BlockService(
    private val blockedUserRepository: BlockedUserRepository,
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val discoveryService: DiscoveryService,
    private val realtimeNotifier: RealtimeNotifier,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun block(userId: UUID, targetId: UUID) {
        if (userId == targetId) throw BadRequestException("SELF_BLOCK", "Нельзя заблокировать себя")
        if (!userRepository.existsById(targetId)) {
            throw NotFoundException("USER_NOT_FOUND", "Пользователь не найден")
        }
        val id = BlockedUserId(blockerId = userId, blockedId = targetId)
        if (!blockedUserRepository.existsById(id)) {
            blockedUserRepository.save(BlockedUser(id))
        }


        val (a, b) = if (userId.toString() < targetId.toString()) userId to targetId else targetId to userId
        matchRepository.findByNormalizedPair(a, b)?.let { match ->
            if (match.active) {
                match.unmatchedAt = Instant.now()
                match.unmatchedBy = userId
                matchRepository.save(match)
                realtimeNotifier.sendEvent(
                    targetId,
                    RealtimeEvent("match.removed", mapOf("matchId" to match.id.toString())),
                )
            }
        }

        discoveryService.removeCandidate(userId, targetId)
        discoveryService.removeCandidate(targetId, userId)
        log.info("{} заблокировал {}", userId, targetId)
    }

    @Transactional
    fun unblock(userId: UUID, targetId: UUID) {
        blockedUserRepository.deleteById(BlockedUserId(blockerId = userId, blockedId = targetId))
        discoveryService.invalidate(userId)
    }

    @Transactional(readOnly = true)
    fun listBlocked(userId: UUID): List<BlockedUserItem> =
        blockedUserRepository.findAllByIdBlockerId(userId).map { blocked ->
            BlockedUserItem(
                userId = blocked.id.blockedId,
                displayName = profileRepository.findById(blocked.id.blockedId).orElse(null)?.displayName,
                blockedAt = blocked.createdAt,
            )
        }
}
