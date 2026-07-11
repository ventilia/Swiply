package com.swiply.backend.matching

import com.swiply.backend.auth.OkResponse
import com.swiply.backend.common.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Blocks", description = "Блокировки пользователей")
class BlockController(private val blockService: BlockService) {

    @PostMapping("/{id}/block")
    @Operation(summary = "Заблокировать: скрывает из ленты, рвёт мэтч, запрещает переписку")
    suspend fun block(@PathVariable id: UUID): OkResponse {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { blockService.block(userId, id) }
        return OkResponse()
    }

    @DeleteMapping("/{id}/block")
    @Operation(summary = "Разблокировать")
    suspend fun unblock(@PathVariable id: UUID): OkResponse {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { blockService.unblock(userId, id) }
        return OkResponse()
    }

    @GetMapping("/me/blocked")
    @Operation(summary = "Мой список заблокированных")
    suspend fun listBlocked(): List<BlockedUserItem> {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { blockService.listBlocked(userId) }
    }
}
