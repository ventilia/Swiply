package com.swiply.backend.matching

import com.swiply.backend.auth.UserRepository
import com.swiply.backend.chat.PresenceService
import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.Cursor
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.common.PageResponse
import com.swiply.backend.common.RateLimiter
import com.swiply.backend.common.UserStatus
import com.swiply.backend.config.SwiplyProperties
import com.swiply.backend.discovery.DiscoveryService
import com.swiply.backend.media.PhotoService
import com.swiply.backend.notification.NotificationService
import com.swiply.backend.notification.NotificationType
import com.swiply.backend.notification.RealtimeEvent
import com.swiply.backend.notification.RealtimeNotifier
import com.swiply.backend.profile.ProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID


@Service
class MatchingService(
    private val swipeRepository: SwipeRepository,
    private val matchRepository: MatchRepository,
    private val blockedUserRepository: BlockedUserRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val photoService: PhotoService,
    private val presenceService: PresenceService,
    private val notificationService: NotificationService,
    private val realtimeNotifier: RealtimeNotifier,
    private val rateLimiter: RateLimiter,
    private val discoveryService: DiscoveryService,
    private val jdbc: NamedParameterJdbcTemplate,
    private val props: SwiplyProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val UNDO_WINDOW: Duration = Duration.ofMinutes(5)
    }

    @Transactional
    fun swipe(userId: UUID, request: SwipeRequest): SwipeResponse {
        if (request.toUserId == userId) {
            throw BadRequestException("SELF_SWIPE", "Нельзя свайпать самого себя")
        }


        swipeRepository.findByFromUserIdAndToUserId(userId, request.toUserId)?.let {
            return currentStateOf(userId, request.toUserId)
        }

        val target = userRepository.findById(request.toUserId).orElse(null)
        if (target == null || target.status != UserStatus.ACTIVE) {
            throw NotFoundException("USER_NOT_FOUND", "Пользователь не найден")
        }
        if (blockedUserRepository.existsEitherWay(userId, request.toUserId)) {
            throw NotFoundException("USER_NOT_FOUND", "Пользователь не найден")
        }


        when (request.action) {
            SwipeAction.LIKE -> rateLimiter.acquire(
                "swipe", userId.toString(), props.limits.swipesPerDay, Duration.ofDays(1),
            )
            SwipeAction.SUPERLIKE -> rateLimiter.acquire(
                "superlike", userId.toString(), props.limits.superlikesPerDay, Duration.ofDays(1),
            )
            SwipeAction.DISLIKE -> Unit
        }


        lockPair(userId, request.toUserId)

        try {
            swipeRepository.saveAndFlush(Swipe(fromUserId = userId, toUserId = request.toUserId, action = request.action))
        } catch (e: DataIntegrityViolationException) {

            return currentStateOf(userId, request.toUserId)
        }

        discoveryService.removeCandidate(userId, request.toUserId)

        if (request.action == SwipeAction.DISLIKE) {
            return SwipeResponse(matched = false)
        }

        val reverseLike = swipeRepository.findReverseLike(from = request.toUserId, to = userId)
        return if (reverseLike != null) {
            val match = createMatch(userId, request.toUserId)
            SwipeResponse(matched = match != null, matchId = match?.id, remainingLikes = remainingLikes(userId))
        } else {
            notifyLikeReceived(likerId = userId, receiverId = request.toUserId, superlike = request.action == SwipeAction.SUPERLIKE)
            SwipeResponse(matched = false, remainingLikes = remainingLikes(userId))
        }
    }


    @Transactional
    fun undoLastSwipe(userId: UUID): UndoSwipeResponse {
        val last = swipeRepository.findTopByFromUserIdOrderByCreatedAtDesc(userId)
            ?: throw NotFoundException("NOTHING_TO_UNDO", "Нет свайпов для отмены")
        if (Duration.between(last.createdAt, Instant.now()) > UNDO_WINDOW) {
            throw BadRequestException("UNDO_EXPIRED", "Отменить можно только недавний свайп")
        }
        val match = findMatchBetween(userId, last.toUserId)
        if (match != null && match.active) {
            throw BadRequestException("UNDO_MATCHED", "Свайп уже привёл к мэтчу — отменить нельзя")
        }

        swipeRepository.delete(last)
        when (last.action) {
            SwipeAction.LIKE -> rateLimiter.release("swipe", userId.toString())
            SwipeAction.SUPERLIKE -> rateLimiter.release("superlike", userId.toString())
            SwipeAction.DISLIKE -> Unit
        }
        return UndoSwipeResponse(toUserId = last.toUserId, action = last.action)
    }

    @Transactional(readOnly = true)
    fun matches(userId: UUID, cursor: String?, limit: Int): PageResponse<MatchItemResponse> {
        val cursorTs = Cursor.decode(cursor)?.toLongOrNull()?.let { Instant.ofEpochMilli(it) }
        val matches = matchRepository.findActiveFor(userId, cursorTs, PageRequest.of(0, limit + 1))
        val page = matches.take(limit)

        val items = page.mapNotNull { match ->
            val otherId = match.otherUserId(userId)
            val profile = profileRepository.findById(otherId).orElse(null) ?: return@mapNotNull null
            MatchItemResponse(
                matchId = match.id,
                userId = otherId,
                displayName = profile.displayName,
                age = profile.age,
                thumbUrl = photoService.listApprovedFor(otherId).firstOrNull()?.thumbUrl,
                isOnline = presenceService.isOnline(otherId),
                matchedAt = match.matchedAt,
            )
        }
        val next = if (matches.size > limit) {
            Cursor.encode(page.last().matchedAt.toEpochMilli().toString())
        } else {
            null
        }
        return PageResponse(items, next)
    }

    @Transactional
    fun unmatch(userId: UUID, matchId: UUID) {
        val match = matchRepository.findById(matchId).orElse(null)
        if (match == null || !match.involves(userId) || !match.active) {
            throw NotFoundException("MATCH_NOT_FOUND", "Мэтч не найден")
        }
        match.unmatchedAt = Instant.now()
        match.unmatchedBy = userId
        matchRepository.save(match)

        val otherId = match.otherUserId(userId)
        realtimeNotifier.sendEvent(otherId, RealtimeEvent("match.removed", mapOf("matchId" to matchId.toString())))
        log.info("Unmatch {}: {} разорвал пару с {}", matchId, userId, otherId)
    }

    /** «Кто лайкнул тебя»: входящие лайки без моего ответного свайпа. */
    @Transactional(readOnly = true)
    fun likesReceived(userId: UUID, cursor: String?, limit: Int): PageResponse<LikeReceivedItem> {
        val cursorTs = Cursor.decode(cursor)?.toLongOrNull()?.let { Instant.ofEpochMilli(it) }
        val rows = jdbc.query(
            """
            SELECT s.from_user_id, s.action, s.created_at,
                   p.display_name, date_part('year', age(p.birth_date))::int AS age
            FROM swipes s
            JOIN users u ON u.id = s.from_user_id AND u.status = 'ACTIVE'
            JOIN profiles p ON p.user_id = s.from_user_id
            WHERE s.to_user_id = :userId
              AND s.action IN ('LIKE', 'SUPERLIKE')
              AND NOT EXISTS (SELECT 1 FROM swipes s2
                               WHERE s2.from_user_id = :userId AND s2.to_user_id = s.from_user_id)
              AND NOT EXISTS (SELECT 1 FROM blocked_users b
                               WHERE (b.blocker_id = :userId AND b.blocked_id = s.from_user_id)
                                  OR (b.blocker_id = s.from_user_id AND b.blocked_id = :userId))
              AND (CAST(:cursorTs AS timestamptz) IS NULL OR s.created_at < :cursorTs)
            ORDER BY s.created_at DESC
            LIMIT :limitPlusOne
            """.trimIndent(),
            mapOf("userId" to userId, "cursorTs" to cursorTs?.let { java.sql.Timestamp.from(it) }, "limitPlusOne" to limit + 1),
        ) { rs, _ ->
            LikeReceivedItem(
                userId = rs.getObject("from_user_id", UUID::class.java),
                displayName = rs.getString("display_name"),
                age = rs.getInt("age"),
                thumbUrl = null,
                superlike = rs.getString("action") == SwipeAction.SUPERLIKE.name,
                likedAt = rs.getTimestamp("created_at").toInstant(),
            )
        }
        val page = rows.take(limit).map { it.copy(thumbUrl = photoService.listApprovedFor(it.userId).firstOrNull()?.thumbUrl) }
        val next = if (rows.size > limit) Cursor.encode(page.last().likedAt.toEpochMilli().toString()) else null
        return PageResponse(page, next)
    }

    // ===== внутреннее =====

    private fun currentStateOf(userId: UUID, otherId: UUID): SwipeResponse {
        val match = findMatchBetween(userId, otherId)
        return SwipeResponse(matched = match?.active == true, matchId = match?.takeIf { it.active }?.id)
    }

    private fun findMatchBetween(a: UUID, b: UUID): Match? {
        val (first, second) = normalizePair(a, b)
        return matchRepository.findByNormalizedPair(first, second)
    }


    private fun normalizePair(a: UUID, b: UUID): Pair<UUID, UUID> =
        if (a.toString() < b.toString()) a to b else b to a

    private fun createMatch(userId: UUID, otherId: UUID): Match? {
        val (a, b) = normalizePair(userId, otherId)
        val existing = matchRepository.findByNormalizedPair(a, b)
        if (existing != null) {
            // активный — идемпотентный повтор; разорванный — unmatch окончателен
            return existing.takeIf { it.active }
        }
        val match = matchRepository.save(Match(userAId = a, userBId = b))

        // Уведомляем обоих: inbox (переживает офлайн) + realtime + push-задача
        notifyMatch(match, recipientId = userId, otherId = otherId)
        notifyMatch(match, recipientId = otherId, otherId = userId)
        log.info("Мэтч {} создан: {} + {}", match.id, a, b)
        return match
    }

    private fun notifyMatch(match: Match, recipientId: UUID, otherId: UUID) {
        val profile = profileRepository.findById(otherId).orElse(null)
        val thumb = photoService.listApprovedFor(otherId).firstOrNull()?.thumbUrl
        val payload = mapOf(
            "matchId" to match.id.toString(),
            "userId" to otherId.toString(),
            "displayName" to (profile?.displayName ?: ""),
            "thumbUrl" to thumb,
        )
        notificationService.notify(
            userId = recipientId,
            type = NotificationType.NEW_MATCH,
            payload = payload,
            pushTitle = "Новый мэтч!",
            pushBody = "Вы понравились друг другу с ${profile?.displayName ?: "кем-то"}",
        )
        realtimeNotifier.sendEvent(recipientId, RealtimeEvent("match.created", payload))
    }

    private fun notifyLikeReceived(likerId: UUID, receiverId: UUID, superlike: Boolean) {
        notificationService.notify(
            userId = receiverId,
            type = NotificationType.NEW_LIKE,
            payload = mapOf("superlike" to superlike),
            pushTitle = if (superlike) "Суперлайк!" else "Новый лайк",
            pushBody = "Кому-то понравился твой профиль",
        )
        realtimeNotifier.sendEvent(receiverId, RealtimeEvent("like.received", mapOf("superlike" to superlike)))
        log.debug("Лайк от {} к {} (superlike={})", likerId, receiverId, superlike)
    }

    private fun remainingLikes(userId: UUID): Long =
        rateLimiter.remaining("swipe", userId.toString(), props.limits.swipesPerDay)

    private fun lockPair(a: UUID, b: UUID) {
        val (first, second) = normalizePair(a, b)
        jdbc.queryForObject(
            "SELECT pg_advisory_xact_lock(hashtextextended(:pair, 0))",
            mapOf("pair" to "$first:$second"),
            Any::class.java,
        )
    }
}
