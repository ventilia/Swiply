package com.swiply.backend.profile

import com.swiply.backend.auth.UserRepository
import com.swiply.backend.chat.PresenceService
import com.swiply.backend.common.AfterCommitPublisher
import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.Gender
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.common.UserStatus
import com.swiply.backend.config.RabbitConfig
import com.swiply.backend.discovery.DiscoveryRecomputeTask
import com.swiply.backend.media.PhotoService
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

@Service
class ProfileService(
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val photoService: PhotoService,
    private val presenceService: PresenceService,
    private val jdbc: NamedParameterJdbcTemplate,
    private val publisher: AfterCommitPublisher,
) {

    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    @Transactional(readOnly = true)
    fun getMe(userId: UUID): MyProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("USER_NOT_FOUND", "Пользователь не найден") }
        val profile = profileRepository.findById(userId)
            .orElseThrow { NotFoundException("PROFILE_NOT_FOUND", "Профиль не найден") }
        return MyProfileResponse(
            id = user.id,
            email = user.email,
            emailVerified = user.emailVerified,
            displayName = profile.displayName,
            birthDate = profile.birthDate,
            age = profile.age,
            gender = profile.gender,
            interestedIn = profile.interestedIn.map { Gender.valueOf(it) }.toSet(),
            interests = profile.interests.toList(),
            bio = profile.bio,
            city = profile.city,
            location = profile.location?.let { LocationView(latitude = it.y, longitude = it.x) },
            minAgePref = profile.minAgePref,
            maxAgePref = profile.maxAgePref,
            maxDistanceKm = profile.maxDistanceKm,
            isIncognito = profile.isIncognito,
            isDiscoverable = profile.isDiscoverable,
            isVerified = profile.isVerified,
            photos = photoService.listFor(userId),
        )
    }

    @Transactional
    fun updateMe(userId: UUID, request: UpdateProfileRequest): MyProfileResponse {
        val profile = profileRepository.findById(userId)
            .orElseThrow { NotFoundException("PROFILE_NOT_FOUND", "Профиль не найден") }

        request.displayName?.let { profile.displayName = it.trim() }
        request.bio?.let { profile.bio = it.trim().ifBlank { null } }
        request.gender?.let { profile.gender = it }
        request.interestedIn?.let {
            if (it.isEmpty()) throw BadRequestException("EMPTY_INTERESTED_IN", "Выберите, кого показывать")
            profile.interestedIn = it.map { g -> g.name }.toMutableList()
        }
        request.interests?.let { profile.interests = Interests.sanitize(it) }
        request.city?.let { profile.city = it.trim().ifBlank { null } }

        profileRepository.save(profile)
        requestRecompute(userId)
        return getMe(userId)
    }

    @Transactional
    fun updatePreferences(userId: UUID, request: UpdatePreferencesRequest): MyProfileResponse {
        val profile = profileRepository.findById(userId)
            .orElseThrow { NotFoundException("PROFILE_NOT_FOUND", "Профиль не найден") }

        request.minAge?.let { profile.minAgePref = it }
        request.maxAge?.let { profile.maxAgePref = it }
        if (profile.minAgePref > profile.maxAgePref) {
            throw BadRequestException("BAD_AGE_RANGE", "Минимальный возраст больше максимального")
        }
        request.maxDistanceKm?.let { profile.maxDistanceKm = it }
        request.isIncognito?.let { profile.isIncognito = it }
        request.isDiscoverable?.let { profile.isDiscoverable = it }

        profileRepository.save(profile)
        requestRecompute(userId)
        return getMe(userId)
    }

    @Transactional
    fun updateLocation(userId: UUID, request: UpdateLocationRequest) {
        val profile = profileRepository.findById(userId)
            .orElseThrow { NotFoundException("PROFILE_NOT_FOUND", "Профиль не найден") }

        // JTS: x = longitude, y = latitude
        profile.location = geometryFactory.createPoint(Coordinate(request.longitude, request.latitude))
        request.city?.let { profile.city = it.trim().ifBlank { null } }
        profileRepository.save(profile)

        userRepository.findById(userId).ifPresent {
            it.lastActiveAt = Instant.now()
            userRepository.save(it)
        }
        requestRecompute(userId)
    }


    @Transactional(readOnly = true)
    fun getPublicProfile(viewerId: UUID, targetId: UUID): PublicProfileResponse {
        if (viewerId == targetId) {
            throw BadRequestException("USE_ME_ENDPOINT", "Для своего профиля используйте /users/me")
        }
        val user = userRepository.findById(targetId).orElse(null)
        if (user == null || user.status == UserStatus.BANNED || user.status == UserStatus.DELETED) {
            throw NotFoundException("USER_NOT_FOUND", "Пользователь не найден")
        }
        val profile = profileRepository.findById(targetId)
            .orElseThrow { NotFoundException("USER_NOT_FOUND", "Пользователь не найден") }

        if (isBlockedEitherWay(viewerId, targetId)) {
            throw NotFoundException("USER_NOT_FOUND", "Пользователь не найден")
        }
        if (!profile.isDiscoverable && !hasActiveMatch(viewerId, targetId)) {
            throw NotFoundException("USER_NOT_FOUND", "Пользователь не найден")
        }

        val presence = presenceService.presenceOf(targetId)
        return PublicProfileResponse(
            id = targetId,
            displayName = profile.displayName,
            age = profile.age,
            gender = profile.gender,
            interests = profile.interests.toList(),
            bio = profile.bio,
            city = profile.city,
            isVerified = profile.isVerified,
            distanceKm = roundedDistanceKm(viewerId, targetId),
            isOnline = presence.online,
            lastSeenAt = presence.lastSeenAt,
            photos = photoService.listApprovedFor(targetId),
        )
    }


    fun roundedDistanceKm(a: UUID, b: UUID): Int? {
        val meters = jdbc.queryForObject(
            """
            SELECT ST_Distance(pa.location::geography, pb.location::geography)
            FROM profiles pa, profiles pb
            WHERE pa.user_id = :a AND pb.user_id = :b
              AND pa.location IS NOT NULL AND pb.location IS NOT NULL
            """.trimIndent(),
            mapOf("a" to a, "b" to b),
        ) { rs, _ -> rs.getDouble(1) }
        return meters?.let { max(1, (it / 1000).roundToInt()) }
    }

    private fun isBlockedEitherWay(a: UUID, b: UUID): Boolean =
        jdbc.queryForObject(
            """
            SELECT EXISTS(
                SELECT 1 FROM blocked_users
                WHERE (blocker_id = :a AND blocked_id = :b) OR (blocker_id = :b AND blocked_id = :a)
            )
            """.trimIndent(),
            mapOf("a" to a, "b" to b),
            Boolean::class.java,
        ) ?: false

    private fun hasActiveMatch(a: UUID, b: UUID): Boolean =
        jdbc.queryForObject(
            """
            SELECT EXISTS(
                SELECT 1 FROM matches
                WHERE ((user_a_id = :a AND user_b_id = :b) OR (user_a_id = :b AND user_b_id = :a))
                  AND unmatched_at IS NULL
            )
            """.trimIndent(),
            mapOf("a" to a, "b" to b),
            Boolean::class.java,
        ) ?: false

    private fun requestRecompute(userId: UUID) {
        publisher.publish(
            RabbitConfig.EXCHANGE,
            RabbitConfig.QUEUE_DISCOVERY_RECOMPUTE,
            DiscoveryRecomputeTask(userId),
        )
    }
}
