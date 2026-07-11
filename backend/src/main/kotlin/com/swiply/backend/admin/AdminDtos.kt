package com.swiply.backend.admin

import com.swiply.backend.common.UserRole
import com.swiply.backend.common.UserStatus
import com.swiply.backend.media.PhotoStatus
import com.swiply.backend.moderation.ReportReason
import com.swiply.backend.moderation.ReportStatus
import java.time.Instant
import java.util.UUID

data class AdminUserItem(
    val id: UUID,
    val email: String,
    val displayName: String?,
    val role: UserRole,
    val status: UserStatus,
    val emailVerified: Boolean,
    val createdAt: Instant,
    val lastActiveAt: Instant?,
)

data class AdminReportItem(
    val id: UUID,
    val reporterId: UUID,
    val reporterEmail: String?,
    val targetUserId: UUID,
    val targetEmail: String?,
    val targetDisplayName: String?,
    val reason: ReportReason,
    val description: String?,
    val status: ReportStatus,
    val createdAt: Instant,
)

data class AdminPhotoItem(
    val id: UUID,
    val userId: UUID,
    val userEmail: String?,
    val url: String,
    val status: PhotoStatus,
    val createdAt: Instant,
)

data class AdminStats(
    val totalUsers: Long,
    val activeUsers: Long,
    val suspendedUsers: Long,
    val bannedUsers: Long,
    val activeMatches: Long,
    val totalSwipes: Long,
    val pendingReports: Long,
    val pendingPhotos: Long,
)

data class AdminUserDetail(
    val id: UUID,
    val email: String,
    val displayName: String?,
    val role: UserRole,
    val status: UserStatus,
    val emailVerified: Boolean,
    val age: Int?,
    val city: String?,
    val bio: String?,
    val interests: List<String>,
    val createdAt: Instant,
    val lastActiveAt: Instant?,
    val photos: List<AdminPhotoItem>,
    val reports: List<AdminReportRow>,
    val history: List<AdminHistoryRow>,
)

data class AdminReportRow(
    val reason: ReportReason,
    val description: String?,
    val status: ReportStatus,
    val createdAt: Instant,
    val reporterEmail: String?,
)

data class AdminHistoryRow(
    val action: String,
    val reason: String?,
    val createdAt: Instant,
)

data class ModerationRequest(
    val reason: String? = null,
)

data class ResolveReportRequest(
    val dismiss: Boolean = false,
    val note: String? = null,
)
