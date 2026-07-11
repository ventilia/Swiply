package com.swiply.backend.matching

import com.swiply.backend.common.PageResponse
import com.swiply.backend.common.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Matching", description = "Свайпы, мэтчи, «кто лайкнул тебя»")
class MatchingController(private val matchingService: MatchingService) {

    @PostMapping("/swipes")
    @Operation(summary = "Свайп. Мэтч детектится синхронно — работает и для офлайн-получателя")
    suspend fun swipe(@Valid @RequestBody request: SwipeRequest): SwipeResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { matchingService.swipe(userId, request) }
    }

    @DeleteMapping("/swipes/last")
    @Operation(summary = "Отмена последнего свайпа (бонус-фича, окно 5 минут)")
    suspend fun undoLastSwipe(): UndoSwipeResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { matchingService.undoLastSwipe(userId) }
    }

    @GetMapping("/matches")
    @Operation(summary = "Активные мэтчи (cursor-пагинация)")
    suspend fun matches(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false, defaultValue = "20") limit: Int,
    ): PageResponse<MatchItemResponse> {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { matchingService.matches(userId, cursor, limit.coerceIn(1, 50)) }
    }

    @DeleteMapping("/matches/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Разорвать мэтч (окончательно)")
    suspend fun unmatch(@PathVariable id: UUID) {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { matchingService.unmatch(userId, id) }
    }

    @GetMapping("/likes/received")
    @Operation(summary = "Кто лайкнул тебя (бонус: блюр-тизер до взаимности)")
    suspend fun likesReceived(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false, defaultValue = "20") limit: Int,
    ): PageResponse<LikeReceivedItem> {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { matchingService.likesReceived(userId, cursor, limit.coerceIn(1, 50)) }
    }
}
