package com.swiply.app.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.swiply.app.core.database.entity.ConversationEntity
import com.swiply.app.core.database.entity.MatchEntity
import com.swiply.app.core.database.entity.MessageEntity
import com.swiply.app.core.database.entity.MyProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY COALESCE(lastMessageSentAt, createdAt) DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun byId(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun observeById(id: String): Flow<ConversationEntity?>

    /** Суммарный unread — для бейджа на табе */
    @Query("SELECT COALESCE(SUM(unreadCount), 0) FROM conversations")
    fun observeUnreadTotal(): Flow<Int>

    @Upsert
    suspend fun upsert(item: ConversationEntity)

    @Upsert
    suspend fun upsertAll(items: List<ConversationEntity>)

    /** Синк с сервером: удаляем локальные диалоги, которых больше нет */
    @Query("DELETE FROM conversations WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun clearUnread(id: String)

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :conversationId")
    suspend fun incrementUnread(conversationId: String)

    @Query("UPDATE conversations SET peerIsOnline = :online WHERE peerUserId = :userId")
    suspend fun updatePresence(userId: String, online: Boolean)

    @Query("DELETE FROM conversations")
    suspend fun clear()
}

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY sentAt DESC, id DESC")
    fun pagingSource(conversationId: String): PagingSource<Int, MessageEntity>

    @Upsert
    suspend fun upsert(item: MessageEntity)

    @Upsert
    suspend fun upsertAll(items: List<MessageEntity>)

    /** Подтверждение доставки: заменяем optimistic-сообщение серверным */
    @Query("DELETE FROM messages WHERE clientTempId = :tempId AND isPending = 1")
    suspend fun deletePendingByTempId(tempId: String)

    /** Неотправленные (WS был оборван) — ресендятся при reconnect */
    @Query("SELECT * FROM messages WHERE isPending = 1 ORDER BY sentAt")
    suspend fun pendingMessages(): List<MessageEntity>

    /** Собеседник прочитал: помечаем мои исходящие */
    @Query(
        "UPDATE messages SET readAt = :readAtMillis, status = 'READ' " +
            "WHERE conversationId = :conversationId AND senderId = :myUserId AND readAt IS NULL",
    )
    suspend fun markMyMessagesRead(conversationId: String, myUserId: String, readAtMillis: Long)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearConversation(conversationId: String)

    @Query("DELETE FROM messages")
    suspend fun clear()
}

@Dao
interface MatchDao {

    @Query("SELECT * FROM matches ORDER BY matchedAt DESC")
    fun observeAll(): Flow<List<MatchEntity>>

    @Upsert
    suspend fun upsertAll(items: List<MatchEntity>)

    @Query("DELETE FROM matches WHERE matchId NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<String>)

    @Query("DELETE FROM matches WHERE matchId = :matchId")
    suspend fun delete(matchId: String)

    @Query("UPDATE matches SET isOnline = :online WHERE userId = :userId")
    suspend fun updatePresence(userId: String, online: Boolean)

    @Query("DELETE FROM matches")
    suspend fun clear()
}

@Dao
interface MyProfileDao {

    @Query("SELECT json FROM my_profile WHERE id = 1")
    fun observe(): Flow<String?>

    @Query("SELECT json FROM my_profile WHERE id = 1")
    suspend fun get(): String?

    @Upsert
    suspend fun upsert(entity: MyProfileEntity)

    @Query("DELETE FROM my_profile")
    suspend fun clear()
}
