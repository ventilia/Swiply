package com.swiply.app.feature.match

import com.swiply.app.core.common.AppResult
import com.swiply.app.core.common.map
import com.swiply.app.core.common.onSuccess
import com.swiply.app.core.database.dao.MatchDao
import com.swiply.app.core.database.toDomain
import com.swiply.app.core.database.toEntity
import com.swiply.app.core.model.LikeReceived
import com.swiply.app.core.model.MatchItem
import com.swiply.app.core.model.Page
import com.swiply.app.core.network.ApiCaller
import com.swiply.app.core.network.api.MatchingApi
import com.swiply.app.core.network.dto.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val matchingApi: MatchingApi,
    private val matchDao: MatchDao,
    private val apiCaller: ApiCaller,
) {

    /** Room — источник для UI; сеть синкает при заходе и по realtime-событиям */
    val matches: Flow<List<MatchItem>> = matchDao.observeAll()
        .map { list -> list.map { it.toDomain() } }

    suspend fun refreshMatches(): AppResult<Unit> =
        apiCaller.call { matchingApi.matches(limit = 50) }
            .onSuccess { page ->
                val items = page.items.map { it.toDomain() }
                matchDao.upsertAll(items.map { it.toEntity() })
                matchDao.deleteNotIn(items.map { it.matchId.toString() })
            }
            .map { }

    suspend fun likesReceived(cursor: String? = null): AppResult<Page<LikeReceived>> =
        apiCaller.call { matchingApi.likesReceived(cursor = cursor, limit = 30) }
            .map { page -> Page(page.items.map { it.toDomain() }, page.nextCursor) }

    suspend fun unmatch(matchId: UUID): AppResult<Unit> {
        val result = apiCaller.call { matchingApi.unmatch(matchId.toString()) }
        if (result is AppResult.Success) {
            matchDao.delete(matchId.toString())
        }
        return result.map { }
    }
}
