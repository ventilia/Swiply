package com.swiply.backend.chat

import com.swiply.backend.auth.OkResponse
import com.swiply.backend.common.PageResponse
import com.swiply.backend.common.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Chat", description = "Диалоги и история сообщений (MongoDB)")
class ChatRestController(private val chatService: ChatService) {

    @GetMapping
    @Operation(summary = "Мои диалоги с превью последнего сообщения и unread-счётчиком")
    suspend fun conversations(): List<ConversationResponse> {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { chatService.conversations(userId) }
    }

    @PostMapping("/by-match/{matchId}")
    @Operation(summary = "Диалог по мэтчу (get-or-create) — точка входа с экрана мэтчей")
    suspend fun byMatch(@PathVariable matchId: UUID): ConversationResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { chatService.conversationByMatch(userId, matchId) }
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "История сообщений (cursor по sentAt+id, новые сверху)")
    suspend fun messages(
        @PathVariable id: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false, defaultValue = "30") limit: Int,
    ): PageResponse<MessageResponse> {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { chatService.messages(userId, id, cursor, limit.coerceIn(1, 100)) }
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Пометить диалог прочитанным (REST-дубль chat.read)")
    suspend fun markRead(@PathVariable id: String): OkResponse {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { chatService.markRead(userId, id) }
        return OkResponse()
    }

    @PostMapping("/{id}/attachments", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Загрузка фото-вложения (тот же пайплайн безопасности, что у фото профиля)")
    suspend fun uploadAttachment(
        @PathVariable id: String,
        @RequestPart("file") file: MultipartFile,
    ): AttachmentResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { chatService.uploadAttachment(userId, id, file) }
    }
}
