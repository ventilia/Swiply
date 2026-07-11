package com.swiply.backend.notification

import com.swiply.backend.auth.OkResponse
import com.swiply.backend.common.PageResponse
import com.swiply.backend.common.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Персистентный inbox уведомлений")
class NotificationController(private val notificationService: NotificationService) {

    @GetMapping
    @Operation(summary = "Лента уведомлений (cursor-пагинация)")
    suspend fun list(
        @RequestParam(required = false, defaultValue = "false") unread: Boolean,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false, defaultValue = "20") limit: Int,
    ): PageResponse<NotificationResponse> {
        val userId = SecurityUtils.currentUserId()
        val safeLimit = limit.coerceIn(1, 50)
        return withContext(Dispatchers.IO) { notificationService.list(userId, unread, cursor, safeLimit) }
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Количество непрочитанных (для бейджа)")
    suspend fun unreadCount(): UnreadCountResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { notificationService.unreadCount(userId) }
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Пометить уведомление прочитанным")
    suspend fun markRead(@PathVariable id: UUID): OkResponse {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { notificationService.markRead(userId, id) }
        return OkResponse()
    }

    @PostMapping("/read-all")
    @Operation(summary = "Пометить все прочитанными")
    suspend fun markAllRead(): OkResponse {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { notificationService.markAllRead(userId) }
        return OkResponse()
    }
}
