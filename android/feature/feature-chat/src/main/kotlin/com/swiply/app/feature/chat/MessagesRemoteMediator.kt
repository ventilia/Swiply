package com.swiply.app.feature.chat

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.swiply.app.core.database.dao.MessageDao
import com.swiply.app.core.database.entity.MessageEntity
import com.swiply.app.core.database.toEntity
import com.swiply.app.core.network.api.ChatApi
import com.swiply.app.core.network.dto.toDomain

/**
 * Room + сеть для истории чата: REFRESH тянет свежую страницу,
 * APPEND — более старые по курсору. PREPEND не нужен — новые сообщения
 * приходят по WebSocket и упираются в Room напрямую.
 */
@OptIn(ExperimentalPagingApi::class)
class MessagesRemoteMediator(
    private val conversationId: String,
    private val chatApi: ChatApi,
    private val messageDao: MessageDao,
    private val repository: ChatRepository,
) : RemoteMediator<Int, MessageEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MessageEntity>,
    ): MediatorResult {
        val cursor = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val next = repository.cursors[conversationId]
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                next
            }
        }

        return try {
            val page = chatApi.messages(conversationId, cursor = cursor, limit = 30)
            repository.cursors[conversationId] = page.nextCursor

            // clear не делаем: optimistic pending-сообщения должны пережить REFRESH
            messageDao.upsertAll(page.items.map { it.toDomain().toEntity() })

            MediatorResult.Success(endOfPaginationReached = page.nextCursor == null)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
