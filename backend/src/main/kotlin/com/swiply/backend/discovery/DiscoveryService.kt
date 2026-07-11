package com.swiply.backend.discovery

import com.swiply.backend.chat.PresenceService
import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.Cursor
import com.swiply.backend.common.Gender
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.common.PageResponse
import com.swiply.backend.config.SwiplyProperties
import com.swiply.backend.media.PhotoResponse
import com.swiply.backend.media.PhotoService
import com.swiply.backend.profile.ProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

data class CandidateResponse(
    val userId: UUID,
    val displayName: String,
    val age: Int,
    val gender: Gender,
    val interests: List<String>,
    val bio: String?,
    val city: String?,
    val distanceKm: Int,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val photos: List<PhotoResponse>,
)


@Service
class DiscoveryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val redis: StringRedisTemplate,
    private val ranker: CandidateRanker,
    private val profileRepository: ProfileRepository,
    private val photoService: PhotoService,
    private val presenceService: PresenceService,
    private val props: SwiplyProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_CANDIDATES = 500

        private val CANDIDATES_SQL = """
            SELECT p.user_id,
                   ST_Distance(me.location::geography, p.location::geography) AS distance_meters,
                   me.max_distance_km * 1000.0                                AS max_distance_meters,
                   u.last_active_at,
                   (p.bio IS NOT NULL AND length(trim(p.bio)) > 0)            AS has_bio,
                   (SELECT count(*) FROM photos ph
                     WHERE ph.user_id = p.user_id AND ph.status = 'APPROVED') AS photo_count,
                   p.is_verified
            FROM profiles me
            JOIN profiles p ON p.user_id <> me.user_id
            JOIN users u ON u.id = p.user_id
            WHERE me.user_id = :userId
              AND me.location IS NOT NULL
              AND p.location IS NOT NULL
              AND u.status = 'ACTIVE'
              AND p.is_discoverable = TRUE
              AND p.is_incognito = FALSE
              AND ST_DWithin(me.location::geography, p.location::geography, me.max_distance_km * 1000)
              AND ST_DWithin(p.location::geography, me.location::geography, p.max_distance_km * 1000)
              AND me.gender::text = ANY (p.interested_in)
              AND p.gender::text = ANY (me.interested_in)
              AND date_part('year', age(p.birth_date)) BETWEEN me.min_age_pref AND me.max_age_pref
              AND date_part('year', age(me.birth_date)) BETWEEN p.min_age_pref AND p.max_age_pref
              AND NOT EXISTS (SELECT 1 FROM swipes s
                               WHERE s.from_user_id = me.user_id AND s.to_user_id = p.user_id)
              AND NOT EXISTS (SELECT 1 FROM blocked_users b
                               WHERE (b.blocker_id = me.user_id AND b.blocked_id = p.user_id)
                                  OR (b.blocker_id = p.user_id AND b.blocked_id = me.user_id))
              AND NOT EXISTS (SELECT 1 FROM matches m
                               WHERE ((m.user_a_id = me.user_id AND m.user_b_id = p.user_id)
                                   OR (m.user_a_id = p.user_id AND m.user_b_id = me.user_id))
                                 AND m.unmatched_at IS NULL)
            LIMIT $MAX_CANDIDATES
        """
    }

    private fun cacheKey(userId: UUID) = "discovery:candidates:$userId"

    @Transactional(readOnly = true)
    fun candidates(userId: UUID, cursor: String?, limit: Int): PageResponse<CandidateResponse> {
        val profile = profileRepository.findById(userId)
            .orElseThrow { NotFoundException("PROFILE_NOT_FOUND", "Профиль не найден") }
        if (profile.location == null) {
            throw BadRequestException("NO_LOCATION", "Сначала передайте геопозицию: PUT /users/me/location")
        }

        val key = cacheKey(userId)
        var total = redis.opsForZSet().size(key) ?: 0
        if (total == 0L) {
            total = recompute(userId)
        }

        val offset = Cursor.decode(cursor)?.toLongOrNull() ?: 0L
        val ids = redis.opsForZSet().reverseRange(key, offset, offset + limit - 1)
            ?.mapNotNull { runCatching { UUID.fromString(it) }.getOrNull() }
            .orEmpty()

        val cards = loadCards(userId, ids)
        val nextCursor = if (offset + limit < total) Cursor.encode((offset + limit).toString()) else null
        return PageResponse(cards, nextCursor)
    }


    fun recompute(userId: UUID): Long {
        val rows = jdbc.query(CANDIDATES_SQL, mapOf("userId" to userId)) { rs, _ ->
            CandidateRow(
                userId = rs.getObject("user_id", UUID::class.java),
                distanceMeters = rs.getDouble("distance_meters"),
                maxDistanceMeters = rs.getDouble("max_distance_meters"),
                lastActiveAt = rs.getTimestamp("last_active_at")?.toInstant(),
                hasBio = rs.getBoolean("has_bio"),
                photoCount = rs.getInt("photo_count"),
                isVerified = rs.getBoolean("is_verified"),
            )
        }
        val key = cacheKey(userId)
        redis.delete(key)
        if (rows.isNotEmpty()) {
            val ops = redis.opsForZSet()
            rows.forEach { row -> ops.add(key, row.userId.toString(), ranker.score(row)) }
        }
        redis.expire(key, props.discovery.cacheTtl)
        log.debug("Кэш кандидатов {} пересчитан: {} человек", userId, rows.size)
        return rows.size.toLong()
    }


    fun removeCandidate(ownerId: UUID, candidateId: UUID) {
        redis.opsForZSet().remove(cacheKey(ownerId), candidateId.toString())
    }

    fun invalidate(userId: UUID) {
        redis.delete(cacheKey(userId))
    }

    fun hasCache(userId: UUID): Boolean = (redis.opsForZSet().size(cacheKey(userId)) ?: 0) > 0


    private fun loadCards(viewerId: UUID, ids: List<UUID>): List<CandidateResponse> {
        if (ids.isEmpty()) return emptyList()
        val rows = jdbc.query(
            """
            SELECT p.user_id, p.display_name, p.birth_date, p.gender, p.bio, p.city, p.is_verified, p.interests,
                   date_part('year', age(p.birth_date))::int AS age,
                   ST_Distance(me.location::geography, p.location::geography) AS distance_meters
            FROM profiles me
            JOIN profiles p ON p.user_id IN (:ids)
            JOIN users u ON u.id = p.user_id
            WHERE me.user_id = :viewerId
              AND u.status = 'ACTIVE'
              AND p.is_discoverable = TRUE
              AND NOT EXISTS (SELECT 1 FROM swipes s
                               WHERE s.from_user_id = :viewerId AND s.to_user_id = p.user_id)
              AND NOT EXISTS (SELECT 1 FROM blocked_users b
                               WHERE (b.blocker_id = :viewerId AND b.blocked_id = p.user_id)
                                  OR (b.blocker_id = p.user_id AND b.blocked_id = :viewerId))
            """.trimIndent(),
            mapOf("ids" to ids, "viewerId" to viewerId),
        ) { rs, _ ->
            val userId = rs.getObject("user_id", UUID::class.java)
            @Suppress("UNCHECKED_CAST")
            val interests = (rs.getArray("interests")?.array as? Array<String>)?.toList() ?: emptyList()
            CandidateResponse(
                userId = userId,
                displayName = rs.getString("display_name"),
                age = rs.getInt("age"),
                gender = Gender.valueOf(rs.getString("gender")),
                interests = interests,
                bio = rs.getString("bio"),
                city = rs.getString("city"),
                distanceKm = max(1, (rs.getDouble("distance_meters") / 1000).roundToInt()),
                isVerified = rs.getBoolean("is_verified"),
                isOnline = false,
                photos = emptyList(),
            )
        }
        val byId = rows.associateBy { it.userId }
        // порядок из sorted set + обогащение фото/presence
        return ids.mapNotNull { byId[it] }.map { card ->
            card.copy(
                isOnline = presenceService.isOnline(card.userId),
                photos = photoService.listApprovedFor(card.userId),
            )
        }
    }
}
