package com.swiply.backend.moderation

import com.swiply.backend.auth.RefreshTokenRepository
import com.swiply.backend.auth.TokenBlacklistService
import com.swiply.backend.auth.UserRepository
import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.common.RateLimiter
import com.swiply.backend.common.UserStatus
import com.swiply.backend.config.SwiplyProperties
import com.swiply.backend.media.PhotoRepository
import com.swiply.backend.media.PhotoStatus
import com.swiply.backend.notification.NotificationService
import com.swiply.backend.notification.NotificationType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

data class ReportRequest(
    @field:NotNull
    val targetUserId: UUID,

    @field:NotNull
    val reason: ReportReason,

    @field:Size(max = 1000)
    val description: String? = null,
)

@Service
class ModerationService(
    private val reportRepository: ReportRepository,
    private val actionRepository: ModerationActionRepository,
    private val userRepository: UserRepository,
    private val photoRepository: PhotoRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val blacklist: TokenBlacklistService,
    private val notificationService: NotificationService,
    private val rateLimiter: RateLimiter,
    private val props: SwiplyProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)


    @Transactional
    fun submitReport(reporterId: UUID, request: ReportRequest): UUID {
        if (reporterId == request.targetUserId) {
            throw BadRequestException("SELF_REPORT", "Нельзя пожаловаться на себя")
        }
        if (!userRepository.existsById(request.targetUserId)) {
            throw NotFoundException("USER_NOT_FOUND", "Пользователь не найден")
        }
        rateLimiter.acquire("report", reporterId.toString(), props.limits.reportsPerDay, Duration.ofDays(1))

        val report = reportRepository.save(
            Report(
                reporterId = reporterId,
                targetUserId = request.targetUserId,
                reason = request.reason,
                description = request.description?.trim()?.ifBlank { null },
            ),
        )
        log.info("Репорт {}: {} → {} ({})", report.id, reporterId, request.targetUserId, request.reason)
        return report.id
    }



    @Transactional
    fun warn(moderatorId: UUID, targetId: UUID, reason: String?) {
        requireTarget(targetId)
        audit(moderatorId, targetId, ModerationActionType.WARN, reason)
        notificationService.notify(
            userId = targetId,
            type = NotificationType.MODERATION,
            payload = mapOf("action" to "WARN", "reason" to (reason ?: "")),
            pushTitle = "Предупреждение модерации",
            pushBody = reason ?: "Соблюдайте правила сообщества",
        )
    }

    @Transactional
    fun suspend(moderatorId: UUID, targetId: UUID, reason: String?) {
        changeStatus(moderatorId, targetId, UserStatus.SUSPENDED, ModerationActionType.SUSPEND, reason)
    }

    @Transactional
    fun ban(moderatorId: UUID, targetId: UUID, reason: String?) {
        changeStatus(moderatorId, targetId, UserStatus.BANNED, ModerationActionType.BAN, reason)
    }

    @Transactional
    fun unban(moderatorId: UUID, targetId: UUID, reason: String?) {
        val user = requireTarget(targetId)
        user.status = UserStatus.ACTIVE
        userRepository.save(user)
        blacklist.unmarkUserBlocked(targetId)
        audit(moderatorId, targetId, ModerationActionType.UNBAN, reason)
        notificationService.notify(
            userId = targetId,
            type = NotificationType.MODERATION,
            payload = mapOf("action" to "UNBAN"),
            pushTitle = "Аккаунт восстановлен",
            pushBody = "Доступ к Swiply снова открыт",
        )
    }

    @Transactional
    fun resolveReport(moderatorId: UUID, reportId: UUID, dismiss: Boolean, note: String?) {
        val report = reportRepository.findById(reportId)
            .orElseThrow { NotFoundException("REPORT_NOT_FOUND", "Репорт не найден") }
        if (report.status != ReportStatus.PENDING) {
            throw BadRequestException("ALREADY_RESOLVED", "Репорт уже рассмотрен")
        }
        report.status = if (dismiss) ReportStatus.DISMISSED else ReportStatus.ACTIONED
        report.reviewedBy = moderatorId
        report.reviewedAt = java.time.Instant.now()
        reportRepository.save(report)
        log.info("Репорт {} закрыт модератором {}: {}", reportId, moderatorId, report.status)
    }

    @Transactional
    fun approvePhoto(moderatorId: UUID, photoId: UUID) {
        val photo = photoRepository.findById(photoId)
            .orElseThrow { NotFoundException("PHOTO_NOT_FOUND", "Фото не найдено") }
        photo.status = PhotoStatus.APPROVED
        photoRepository.save(photo)
        audit(moderatorId, photo.userId, ModerationActionType.PHOTO_APPROVE, "photo=$photoId")
    }

    @Transactional
    fun rejectPhoto(moderatorId: UUID, photoId: UUID) {
        val photo = photoRepository.findById(photoId)
            .orElseThrow { NotFoundException("PHOTO_NOT_FOUND", "Фото не найдено") }
        photo.status = PhotoStatus.REJECTED
        photoRepository.save(photo)
        audit(moderatorId, photo.userId, ModerationActionType.PHOTO_REJECT, "photo=$photoId")
        notificationService.notify(
            userId = photo.userId,
            type = NotificationType.MODERATION,
            payload = mapOf("action" to "PHOTO_REJECT", "photoId" to photoId.toString()),
            pushTitle = "Фото отклонено",
            pushBody = "Одно из ваших фото не прошло модерацию",
        )
    }

    fun historyFor(targetId: UUID): List<ModerationAction> =
        actionRepository.findByTargetUserIdOrderByCreatedAtDesc(targetId)



    private fun changeStatus(
        moderatorId: UUID,
        targetId: UUID,
        status: UserStatus,
        actionType: ModerationActionType,
        reason: String?,
    ) {
        val user = requireTarget(targetId)
        if (user.role != com.swiply.backend.common.UserRole.USER) {
            throw BadRequestException("TARGET_IS_STAFF", "Нельзя применить действие к модератору/админу")
        }
        user.status = status
        userRepository.save(user)
        refreshTokenRepository.revokeAllForUser(targetId)
        blacklist.markUserBlocked(targetId, props.jwt.accessTtl)

        audit(moderatorId, targetId, actionType, reason)
        log.warn("Модератор {} применил {} к {}: {}", moderatorId, actionType, targetId, reason ?: "-")
    }

    private fun requireTarget(targetId: UUID) =
        userRepository.findById(targetId)
            .orElseThrow { NotFoundException("USER_NOT_FOUND", "Пользователь не найден") }

    private fun audit(moderatorId: UUID, targetId: UUID, action: ModerationActionType, reason: String?) {
        actionRepository.save(
            ModerationAction(
                moderatorId = moderatorId,
                targetUserId = targetId,
                action = action,
                reason = reason?.take(500),
            ),
        )
    }
}
