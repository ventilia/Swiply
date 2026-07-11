package com.swiply.backend.admin

import com.swiply.backend.auth.OkResponse
import com.swiply.backend.common.SecurityUtils
import com.swiply.backend.common.UserStatus
import com.swiply.backend.media.PhotoStatus
import com.swiply.backend.moderation.ModerationAction
import com.swiply.backend.moderation.ModerationService
import com.swiply.backend.moderation.ReportStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
@Tag(name = "Admin", description = "Модерация и администрирование")
class AdminApiController(
    private val queryService: AdminQueryService,
    private val moderationService: ModerationService,
) {

    @GetMapping("/stats")
    @Operation(summary = "Сводка по системе")
    suspend fun stats(): AdminStats = withContext(Dispatchers.IO) { queryService.stats() }

    @GetMapping("/users")
    @Operation(summary = "Поиск пользователей по email/имени и статусу")
    suspend fun users(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) status: UserStatus?,
        @RequestParam(required = false, defaultValue = "0") offset: Int,
        @RequestParam(required = false, defaultValue = "25") limit: Int,
    ): List<AdminUserItem> = withContext(Dispatchers.IO) {
        queryService.searchUsers(query, status, offset.coerceAtLeast(0), limit.coerceIn(1, 100))
    }

    @PostMapping("/users/{id}/warn")
    @Operation(summary = "Предупреждение пользователю")
    suspend fun warn(@PathVariable id: UUID, @RequestBody(required = false) body: ModerationRequest?): OkResponse {
        val moderatorId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { moderationService.warn(moderatorId, id, body?.reason) }
        return OkResponse()
    }

    @PostMapping("/users/{id}/suspend")
    @Operation(summary = "Приостановить аккаунт (мгновенно рвёт сессии)")
    suspend fun suspend(@PathVariable id: UUID, @RequestBody(required = false) body: ModerationRequest?): OkResponse {
        val moderatorId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { moderationService.suspend(moderatorId, id, body?.reason) }
        return OkResponse()
    }

    @PostMapping("/users/{id}/ban")
    @Operation(summary = "Забанить (мгновенно рвёт сессии)")
    suspend fun ban(@PathVariable id: UUID, @RequestBody(required = false) body: ModerationRequest?): OkResponse {
        val moderatorId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { moderationService.ban(moderatorId, id, body?.reason) }
        return OkResponse()
    }

    @PostMapping("/users/{id}/unban")
    @Operation(summary = "Разбанить / снять приостановку")
    suspend fun unban(@PathVariable id: UUID, @RequestBody(required = false) body: ModerationRequest?): OkResponse {
        val moderatorId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { moderationService.unban(moderatorId, id, body?.reason) }
        return OkResponse()
    }

    @GetMapping("/users/{id}/moderation-history")
    @Operation(summary = "Аудит-лог действий по пользователю")
    suspend fun history(@PathVariable id: UUID): List<ModerationHistoryItem> =
        withContext(Dispatchers.IO) {
            moderationService.historyFor(id).map { it.toHistoryItem() }
        }

    @GetMapping("/users/{id}/detail")
    @Operation(summary = "Карточка пользователя для модерации: профиль, все фото, жалобы, история")
    suspend fun detail(@PathVariable id: UUID): AdminUserDetail =
        withContext(Dispatchers.IO) {
            queryService.userDetail(id)
                ?: throw com.swiply.backend.common.NotFoundException("USER_NOT_FOUND", "Пользователь не найден")
        }

    @GetMapping("/reports")
    @Operation(summary = "Очередь репортов")
    suspend fun reports(
        @RequestParam(required = false, defaultValue = "PENDING") status: ReportStatus,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") limit: Int,
    ): List<AdminReportItem> = withContext(Dispatchers.IO) {
        queryService.reports(status, page.coerceAtLeast(0), limit.coerceIn(1, 100))
    }

    @PostMapping("/reports/{id}/resolve")
    @Operation(summary = "Закрыть репорт (dismiss=true — отклонить)")
    suspend fun resolveReport(@PathVariable id: UUID, @RequestBody body: ResolveReportRequest): OkResponse {
        val moderatorId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { moderationService.resolveReport(moderatorId, id, body.dismiss, body.note) }
        return OkResponse()
    }

    @GetMapping("/photos")
    @Operation(summary = "Очередь фото на модерацию")
    suspend fun photos(
        @RequestParam(required = false, defaultValue = "PENDING") status: PhotoStatus,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") limit: Int,
    ): List<AdminPhotoItem> = withContext(Dispatchers.IO) {
        queryService.photosForModeration(status, page.coerceAtLeast(0), limit.coerceIn(1, 100))
    }

    @PostMapping("/photos/{id}/approve")
    @Operation(summary = "Одобрить фото")
    suspend fun approvePhoto(@PathVariable id: UUID): OkResponse {
        val moderatorId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { moderationService.approvePhoto(moderatorId, id) }
        return OkResponse()
    }

    @PostMapping("/photos/{id}/reject")
    @Operation(summary = "Отклонить фото")
    suspend fun rejectPhoto(@PathVariable id: UUID): OkResponse {
        val moderatorId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { moderationService.rejectPhoto(moderatorId, id) }
        return OkResponse()
    }
}

data class ModerationHistoryItem(
    val id: UUID,
    val moderatorId: UUID,
    val action: String,
    val reason: String?,
    val createdAt: java.time.Instant,
)

private fun ModerationAction.toHistoryItem() = ModerationHistoryItem(
    id = id,
    moderatorId = moderatorId,
    action = action.name,
    reason = reason,
    createdAt = createdAt,
)
