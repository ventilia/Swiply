package com.swiply.backend.discovery

import com.swiply.backend.common.PageResponse
import com.swiply.backend.common.SecurityUtils
import com.swiply.backend.config.SwiplyProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/discovery")
@Tag(name = "Discovery", description = "Лента кандидатов для свайпа")
class DiscoveryController(
    private val discoveryService: DiscoveryService,
    private val props: SwiplyProperties,
) {

    @GetMapping("/candidates")
    @Operation(summary = "Кандидаты поблизости: PostGIS-гео + взаимные фильтры + ранжирование")
    suspend fun candidates(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false, defaultValue = "20") limit: Int,
    ): PageResponse<CandidateResponse> {
        val userId = SecurityUtils.currentUserId()
        val safeLimit = limit.coerceIn(1, props.discovery.pageSizeMax)
        return withContext(Dispatchers.IO) { discoveryService.candidates(userId, cursor, safeLimit) }
    }
}
