package com.swiply.backend.moderation

import com.swiply.backend.common.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class ReportCreatedResponse(val reportId: UUID)

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Жалобы на пользователей")
class ReportController(private val moderationService: ModerationService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Пожаловаться на пользователя (лимитировано)")
    suspend fun submit(@Valid @RequestBody request: ReportRequest): ReportCreatedResponse {
        val userId = SecurityUtils.currentUserId()
        val id = withContext(Dispatchers.IO) { moderationService.submitReport(userId, request) }
        return ReportCreatedResponse(id)
    }
}
