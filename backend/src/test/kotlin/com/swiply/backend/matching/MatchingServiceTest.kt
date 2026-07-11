package com.swiply.backend.matching

import com.swiply.backend.auth.User
import com.swiply.backend.auth.UserRepository
import com.swiply.backend.chat.PresenceService
import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.RateLimiter
import com.swiply.backend.config.SwiplyProperties
import com.swiply.backend.discovery.DiscoveryService
import com.swiply.backend.media.PhotoService
import com.swiply.backend.notification.NotificationService
import com.swiply.backend.notification.NotificationType
import com.swiply.backend.notification.RealtimeNotifier
import com.swiply.backend.profile.Profile
import com.swiply.backend.profile.ProfileRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MatchingServiceTest {

    private val swipeRepository: SwipeRepository = mockk(relaxed = true)
    private val matchRepository: MatchRepository = mockk(relaxed = true)
    private val blockedUserRepository: BlockedUserRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val profileRepository: ProfileRepository = mockk(relaxed = true)
    private val photoService: PhotoService = mockk(relaxed = true)
    private val presenceService: PresenceService = mockk(relaxed = true)
    private val notificationService: NotificationService = mockk(relaxed = true)
    private val realtimeNotifier: RealtimeNotifier = mockk(relaxed = true)
    private val rateLimiter: RateLimiter = mockk(relaxed = true)
    private val discoveryService: DiscoveryService = mockk(relaxed = true)
    private val jdbc: NamedParameterJdbcTemplate = mockk(relaxed = true)

    private val props = SwiplyProperties(
        jwt = SwiplyProperties.Jwt("secret-0123456789-0123456789-0123456789-0123456789", "swiply", Duration.ofMinutes(15), Duration.ofDays(30)),
        media = SwiplyProperties.Media("http://x", "http://x", "k", "s", "b", Duration.ofMinutes(30), 1, 6, 2048, listOf(512)),
        discovery = SwiplyProperties.Discovery(Duration.ofMinutes(10), 50, 300),
        limits = SwiplyProperties.Limits(100, 5, 15, 10, 10, 30),
        auth = SwiplyProperties.Auth(Duration.ofHours(24), Duration.ofHours(1)),
        admin = SwiplyProperties.Admin("a@a", "p"),
    )

    private lateinit var service: MatchingService

    private val me = UUID.randomUUID()
    private val other = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        service = MatchingService(
            swipeRepository, matchRepository, blockedUserRepository, userRepository,
            profileRepository, photoService, presenceService, notificationService,
            realtimeNotifier, rateLimiter, discoveryService, jdbc, props,
        )
        every { swipeRepository.findByFromUserIdAndToUserId(me, other) } returns null
        every { userRepository.findById(other) } returns Optional.of(User(id = other, email = "o@t", passwordHash = "h"))
        every { blockedUserRepository.existsEitherWay(any(), any()) } returns false
        every { profileRepository.findById(any()) } answers {
            Optional.of(
                Profile(
                    userId = firstArg(),
                    displayName = "Профиль",
                    birthDate = LocalDate.of(1995, 1, 1),
                    gender = com.swiply.backend.common.Gender.FEMALE,
                    interestedIn = mutableListOf("MALE"),
                ),
            )
        }
        every { photoService.listApprovedFor(any()) } returns emptyList()
        every { matchRepository.save(any()) } answers { firstArg() }
        every { swipeRepository.saveAndFlush(any()) } answers { firstArg() }
    }

    @Test
    fun `свайп самого себя запрещён`() {
        assertThrows<BadRequestException> {
            service.swipe(me, SwipeRequest(toUserId = me, action = SwipeAction.LIKE))
        }
    }

    @Test
    fun `взаимный лайк создаёт мэтч и уведомляет обоих`() {
        every { swipeRepository.findReverseLike(from = other, to = me) } returns
            Swipe(fromUserId = other, toUserId = me, action = SwipeAction.LIKE)
        every { matchRepository.findByNormalizedPair(any(), any()) } returns null

        val matchSlot = slot<Match>()
        every { matchRepository.save(capture(matchSlot)) } answers { firstArg() }

        val result = service.swipe(me, SwipeRequest(toUserId = other, action = SwipeAction.LIKE))

        assertTrue(result.matched)
        assertNotNull(result.matchId)
        // пара нормализована лексикографически — как uuid-порядок в PostgreSQL
        assertTrue(matchSlot.captured.userAId.toString() < matchSlot.captured.userBId.toString())
        verify(exactly = 2) { notificationService.notify(any(), NotificationType.NEW_MATCH, any(), any(), any()) }
        verify(exactly = 2) { realtimeNotifier.sendEvent(any(), match { it.type == "match.created" }) }
    }

    @Test
    fun `лайк без взаимности уведомляет получателя о лайке`() {
        every { swipeRepository.findReverseLike(from = other, to = me) } returns null

        val result = service.swipe(me, SwipeRequest(toUserId = other, action = SwipeAction.LIKE))

        assertFalse(result.matched)
        verify(exactly = 1) { notificationService.notify(other, NotificationType.NEW_LIKE, any(), any(), any()) }
        verify(exactly = 0) { matchRepository.save(any()) }
    }

    @Test
    fun `дизлайк не проверяет взаимность и не создаёт мэтч`() {
        val result = service.swipe(me, SwipeRequest(toUserId = other, action = SwipeAction.DISLIKE))

        assertFalse(result.matched)
        verify(exactly = 0) { swipeRepository.findReverseLike(any(), any()) }
        verify(exactly = 0) { notificationService.notify(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `повторный свайп идемпотентен — возвращает текущее состояние`() {
        every { swipeRepository.findByFromUserIdAndToUserId(me, other) } returns
            Swipe(fromUserId = me, toUserId = other, action = SwipeAction.LIKE)
        val (a, b) = if (me.toString() < other.toString()) me to other else other to me
        every { matchRepository.findByNormalizedPair(a, b) } returns Match(userAId = a, userBId = b)

        val result = service.swipe(me, SwipeRequest(toUserId = other, action = SwipeAction.LIKE))

        assertTrue(result.matched)
        verify(exactly = 0) { swipeRepository.saveAndFlush(any()) }
    }

    @Test
    fun `свайп убирает кандидата из кэша ленты`() {
        every { swipeRepository.findReverseLike(from = other, to = me) } returns null

        service.swipe(me, SwipeRequest(toUserId = other, action = SwipeAction.LIKE))

        verify { discoveryService.removeCandidate(me, other) }
    }

    @Test
    fun `undo вне окна отклоняется`() {
        val old = Swipe(fromUserId = me, toUserId = other, action = SwipeAction.LIKE)
        old.createdAt = Instant.now().minus(Duration.ofMinutes(30))
        every { swipeRepository.findTopByFromUserIdOrderByCreatedAtDesc(me) } returns old

        assertThrows<BadRequestException> { service.undoLastSwipe(me) }
    }

    @Test
    fun `undo свайпа с мэтчем отклоняется`() {
        val recent = Swipe(fromUserId = me, toUserId = other, action = SwipeAction.LIKE)
        recent.createdAt = Instant.now()
        every { swipeRepository.findTopByFromUserIdOrderByCreatedAtDesc(me) } returns recent
        val (a, b) = if (me.toString() < other.toString()) me to other else other to me
        every { matchRepository.findByNormalizedPair(a, b) } returns Match(userAId = a, userBId = b)

        assertThrows<BadRequestException> { service.undoLastSwipe(me) }
    }

    @Test
    fun `успешный undo удаляет свайп и возвращает лимит`() {
        val recent = Swipe(fromUserId = me, toUserId = other, action = SwipeAction.LIKE)
        recent.createdAt = Instant.now()
        every { swipeRepository.findTopByFromUserIdOrderByCreatedAtDesc(me) } returns recent
        every { matchRepository.findByNormalizedPair(any(), any()) } returns null

        val result = service.undoLastSwipe(me)

        assertEquals(other, result.toUserId)
        verify { swipeRepository.delete(recent) }
        verify { rateLimiter.release("swipe", me.toString()) }
    }
}
