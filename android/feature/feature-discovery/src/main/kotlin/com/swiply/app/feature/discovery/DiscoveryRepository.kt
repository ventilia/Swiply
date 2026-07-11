package com.swiply.app.feature.discovery

import com.swiply.app.core.common.AppResult
import com.swiply.app.core.common.map
import com.swiply.app.core.model.Candidate
import com.swiply.app.core.model.SwipeAction
import com.swiply.app.core.model.SwipeResult
import com.swiply.app.core.network.ApiCaller
import com.swiply.app.core.network.api.DiscoveryApi
import com.swiply.app.core.network.api.MatchingApi
import com.swiply.app.core.network.dto.SwipeRequestDto
import com.swiply.app.core.network.dto.toDomain
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Лента кандидатов: cursor-пагинация с префетчем по мере свайпа.
 */
@Singleton
class DiscoveryRepository @Inject constructor(
    private val discoveryApi: DiscoveryApi,
    private val matchingApi: MatchingApi,
    private val apiCaller: ApiCaller,
) {

    private var nextCursor: String? = null
    private var exhausted = false

    val hasMore: Boolean
        get() = !exhausted

    suspend fun loadFirstPage(limit: Int = 10): AppResult<List<Candidate>> {
        nextCursor = null
        exhausted = false
        return loadPage(limit)
    }

    suspend fun loadNextPage(limit: Int = 10): AppResult<List<Candidate>> = loadPage(limit)

    private suspend fun loadPage(limit: Int): AppResult<List<Candidate>> =
        apiCaller.call { discoveryApi.candidates(cursor = nextCursor, limit = limit) }
            .map { page ->
                nextCursor = page.nextCursor
                exhausted = page.nextCursor == null
                page.items.map { it.toDomain() }
            }

    suspend fun swipe(toUserId: UUID, action: SwipeAction): AppResult<SwipeResult> =
        apiCaller.call {
            matchingApi.swipe(SwipeRequestDto(toUserId = toUserId.toString(), action = action.name)).toDomain()
        }

    /** Возвращает id кандидата, которого вернули в стек */
    suspend fun undoLastSwipe(): AppResult<UUID> =
        apiCaller.call { matchingApi.undoLastSwipe() }
            .map { UUID.fromString(it.toUserId) }
}
