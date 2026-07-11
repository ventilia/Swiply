package com.swiply.backend.admin

import com.swiply.backend.common.UserRole
import com.swiply.backend.common.UserStatus
import com.swiply.backend.media.PhotoRepository
import com.swiply.backend.media.PhotoStatus
import com.swiply.backend.media.MediaStorage
import com.swiply.backend.moderation.Report
import com.swiply.backend.moderation.ReportReason
import com.swiply.backend.moderation.ReportRepository
import com.swiply.backend.moderation.ReportStatus
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminQueryService(
    private val jdbc: NamedParameterJdbcTemplate,
    private val reportRepository: ReportRepository,
    private val photoRepository: PhotoRepository,
    private val storage: MediaStorage,
) {

    @Transactional(readOnly = true)
    fun searchUsers(query: String?, status: UserStatus?, offset: Int, limit: Int): List<AdminUserItem> {
        val q = query?.trim()?.ifBlank { null }?.let { "%$it%" }
        return jdbc.query(
            """
            SELECT u.id, u.email, u.role, u.status, u.email_verified, u.created_at, u.last_active_at,
                   p.display_name
            FROM users u
            LEFT JOIN profiles p ON p.user_id = u.id
            WHERE (CAST(:q AS text) IS NULL OR u.email ILIKE :q OR p.display_name ILIKE :q)
              AND (CAST(:status AS text) IS NULL OR u.status = :status)
            ORDER BY u.created_at DESC
            LIMIT :limit OFFSET :offset
            """.trimIndent(),
            mapOf("q" to q, "status" to status?.name, "limit" to limit, "offset" to offset),
        ) { rs, _ ->
            AdminUserItem(
                id = rs.getObject("id", UUID::class.java),
                email = rs.getString("email"),
                displayName = rs.getString("display_name"),
                role = UserRole.valueOf(rs.getString("role")),
                status = UserStatus.valueOf(rs.getString("status")),
                emailVerified = rs.getBoolean("email_verified"),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                lastActiveAt = rs.getTimestamp("last_active_at")?.toInstant(),
            )
        }
    }

    @Transactional(readOnly = true)
    fun reports(status: ReportStatus, page: Int, size: Int): List<AdminReportItem> =
        reportRepository.findByStatusOrderByCreatedAt(status, PageRequest.of(page, size))
            .content
            .map { enrichReport(it) }

    @Transactional(readOnly = true)
    fun photosForModeration(status: PhotoStatus, page: Int, size: Int): List<AdminPhotoItem> =
        photoRepository.findByStatusOrderByCreatedAt(status, PageRequest.of(page, size))
            .content
            .map { photo ->
                AdminPhotoItem(
                    id = photo.id,
                    userId = photo.userId,
                    userEmail = emailOf(photo.userId),
                    url = storage.presignedGetUrl(photo.storageKey),
                    status = photo.status,
                    createdAt = photo.createdAt,
                )
            }


    @Transactional(readOnly = true)
    fun userDetail(userId: UUID): AdminUserDetail? {
        val base = jdbc.query(
            """
            SELECT u.id, u.email, u.role, u.status, u.email_verified, u.created_at, u.last_active_at,
                   p.display_name, p.bio, p.city, p.interests,
                   date_part('year', age(p.birth_date))::int AS age
            FROM users u
            LEFT JOIN profiles p ON p.user_id = u.id
            WHERE u.id = :id
            """.trimIndent(),
            mapOf("id" to userId),
        ) { rs, _ ->
            @Suppress("UNCHECKED_CAST")
            val interests = (rs.getArray("interests")?.array as? Array<String>)?.toList() ?: emptyList()
            AdminUserDetail(
                id = rs.getObject("id", UUID::class.java),
                email = rs.getString("email"),
                displayName = rs.getString("display_name"),
                role = UserRole.valueOf(rs.getString("role")),
                status = UserStatus.valueOf(rs.getString("status")),
                emailVerified = rs.getBoolean("email_verified"),
                age = rs.getObject("age")?.let { (it as Number).toInt() },
                city = rs.getString("city"),
                bio = rs.getString("bio"),
                interests = interests,
                createdAt = rs.getTimestamp("created_at").toInstant(),
                lastActiveAt = rs.getTimestamp("last_active_at")?.toInstant(),
                photos = emptyList(),
                reports = emptyList(),
                history = emptyList(),
            )
        }.firstOrNull() ?: return null


        val photos = photoRepository.findByUserIdOrderByPosition(userId).map { photo ->
            AdminPhotoItem(
                id = photo.id,
                userId = photo.userId,
                userEmail = base.email,
                url = storage.presignedGetUrl(photo.storageKey),
                status = photo.status,
                createdAt = photo.createdAt,
            )
        }
        val reports = jdbc.query(
            """
            SELECT r.reason, r.description, r.status, r.created_at, ru.email AS reporter_email
            FROM reports r LEFT JOIN users ru ON ru.id = r.reporter_id
            WHERE r.target_user_id = :id ORDER BY r.created_at DESC
            """.trimIndent(),
            mapOf("id" to userId),
        ) { rs, _ ->
            AdminReportRow(
                reason = ReportReason.valueOf(rs.getString("reason")),
                description = rs.getString("description"),
                status = ReportStatus.valueOf(rs.getString("status")),
                createdAt = rs.getTimestamp("created_at").toInstant(),
                reporterEmail = rs.getString("reporter_email"),
            )
        }
        val history = jdbc.query(
            """
            SELECT action, reason, created_at FROM moderation_actions
            WHERE target_user_id = :id ORDER BY created_at DESC
            """.trimIndent(),
            mapOf("id" to userId),
        ) { rs, _ ->
            AdminHistoryRow(
                action = rs.getString("action"),
                reason = rs.getString("reason"),
                createdAt = rs.getTimestamp("created_at").toInstant(),
            )
        }
        return base.copy(photos = photos, reports = reports, history = history)
    }

    @Transactional(readOnly = true)
    fun stats(): AdminStats {
        val row = jdbc.queryForMap(
            """
            SELECT
              (SELECT count(*) FROM users)                                        AS total_users,
              (SELECT count(*) FROM users WHERE status = 'ACTIVE')                AS active_users,
              (SELECT count(*) FROM users WHERE status = 'SUSPENDED')             AS suspended_users,
              (SELECT count(*) FROM users WHERE status = 'BANNED')                AS banned_users,
              (SELECT count(*) FROM matches WHERE unmatched_at IS NULL)           AS active_matches,
              (SELECT count(*) FROM swipes)                                       AS total_swipes,
              (SELECT count(*) FROM reports WHERE status = 'PENDING')             AS pending_reports,
              (SELECT count(*) FROM photos WHERE status = 'PENDING')              AS pending_photos
            """.trimIndent(),
            emptyMap<String, Any>(),
        )
        fun n(key: String): Long = (row[key] as Number).toLong()
        return AdminStats(
            totalUsers = n("total_users"),
            activeUsers = n("active_users"),
            suspendedUsers = n("suspended_users"),
            bannedUsers = n("banned_users"),
            activeMatches = n("active_matches"),
            totalSwipes = n("total_swipes"),
            pendingReports = n("pending_reports"),
            pendingPhotos = n("pending_photos"),
        )
    }

    private fun enrichReport(report: Report): AdminReportItem =
        AdminReportItem(
            id = report.id,
            reporterId = report.reporterId,
            reporterEmail = emailOf(report.reporterId),
            targetUserId = report.targetUserId,
            targetEmail = emailOf(report.targetUserId),
            targetDisplayName = jdbc.query(
                "SELECT display_name FROM profiles WHERE user_id = :id",
                mapOf("id" to report.targetUserId),
            ) { rs, _ -> rs.getString(1) }.firstOrNull(),
            reason = report.reason,
            description = report.description,
            status = report.status,
            createdAt = report.createdAt,
        )

    private fun emailOf(userId: UUID): String? =
        jdbc.query("SELECT email FROM users WHERE id = :id", mapOf("id" to userId)) { rs, _ ->
            rs.getString(1)
        }.firstOrNull()
}
