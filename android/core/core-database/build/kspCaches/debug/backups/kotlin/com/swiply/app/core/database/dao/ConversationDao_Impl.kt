package com.swiply.app.core.database.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.swiply.app.core.database.entity.ConversationEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ConversationDao_Impl(
  __db: RoomDatabase,
) : ConversationDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfConversationEntity: EntityUpsertAdapter<ConversationEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfConversationEntity = EntityUpsertAdapter<ConversationEntity>(object : EntityInsertAdapter<ConversationEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `conversations` (`id`,`matchId`,`peerUserId`,`peerDisplayName`,`peerThumbUrl`,`peerIsOnline`,`peerLastSeenAt`,`lastMessagePreview`,`lastMessageSentAt`,`lastMessageSenderId`,`unreadCount`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ConversationEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.matchId)
        statement.bindText(3, entity.peerUserId)
        statement.bindText(4, entity.peerDisplayName)
        val _tmpPeerThumbUrl: String? = entity.peerThumbUrl
        if (_tmpPeerThumbUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpPeerThumbUrl)
        }
        val _tmp: Int = if (entity.peerIsOnline) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmpPeerLastSeenAt: Long? = entity.peerLastSeenAt
        if (_tmpPeerLastSeenAt == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpPeerLastSeenAt)
        }
        val _tmpLastMessagePreview: String? = entity.lastMessagePreview
        if (_tmpLastMessagePreview == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpLastMessagePreview)
        }
        val _tmpLastMessageSentAt: Long? = entity.lastMessageSentAt
        if (_tmpLastMessageSentAt == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpLastMessageSentAt)
        }
        val _tmpLastMessageSenderId: String? = entity.lastMessageSenderId
        if (_tmpLastMessageSenderId == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpLastMessageSenderId)
        }
        statement.bindLong(11, entity.unreadCount.toLong())
        statement.bindLong(12, entity.createdAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<ConversationEntity>() {
      protected override fun createQuery(): String = "UPDATE `conversations` SET `id` = ?,`matchId` = ?,`peerUserId` = ?,`peerDisplayName` = ?,`peerThumbUrl` = ?,`peerIsOnline` = ?,`peerLastSeenAt` = ?,`lastMessagePreview` = ?,`lastMessageSentAt` = ?,`lastMessageSenderId` = ?,`unreadCount` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ConversationEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.matchId)
        statement.bindText(3, entity.peerUserId)
        statement.bindText(4, entity.peerDisplayName)
        val _tmpPeerThumbUrl: String? = entity.peerThumbUrl
        if (_tmpPeerThumbUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpPeerThumbUrl)
        }
        val _tmp: Int = if (entity.peerIsOnline) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmpPeerLastSeenAt: Long? = entity.peerLastSeenAt
        if (_tmpPeerLastSeenAt == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpPeerLastSeenAt)
        }
        val _tmpLastMessagePreview: String? = entity.lastMessagePreview
        if (_tmpLastMessagePreview == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpLastMessagePreview)
        }
        val _tmpLastMessageSentAt: Long? = entity.lastMessageSentAt
        if (_tmpLastMessageSentAt == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpLastMessageSentAt)
        }
        val _tmpLastMessageSenderId: String? = entity.lastMessageSenderId
        if (_tmpLastMessageSenderId == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpLastMessageSenderId)
        }
        statement.bindLong(11, entity.unreadCount.toLong())
        statement.bindLong(12, entity.createdAt)
        statement.bindText(13, entity.id)
      }
    })
  }

  public override suspend fun upsert(item: ConversationEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfConversationEntity.upsert(_connection, item)
  }

  public override suspend fun upsertAll(items: List<ConversationEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfConversationEntity.upsert(_connection, items)
  }

  public override fun observeAll(): Flow<List<ConversationEntity>> {
    val _sql: String = "SELECT * FROM conversations ORDER BY COALESCE(lastMessageSentAt, createdAt) DESC"
    return createFlow(__db, false, arrayOf("conversations")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMatchId: Int = getColumnIndexOrThrow(_stmt, "matchId")
        val _columnIndexOfPeerUserId: Int = getColumnIndexOrThrow(_stmt, "peerUserId")
        val _columnIndexOfPeerDisplayName: Int = getColumnIndexOrThrow(_stmt, "peerDisplayName")
        val _columnIndexOfPeerThumbUrl: Int = getColumnIndexOrThrow(_stmt, "peerThumbUrl")
        val _columnIndexOfPeerIsOnline: Int = getColumnIndexOrThrow(_stmt, "peerIsOnline")
        val _columnIndexOfPeerLastSeenAt: Int = getColumnIndexOrThrow(_stmt, "peerLastSeenAt")
        val _columnIndexOfLastMessagePreview: Int = getColumnIndexOrThrow(_stmt, "lastMessagePreview")
        val _columnIndexOfLastMessageSentAt: Int = getColumnIndexOrThrow(_stmt, "lastMessageSentAt")
        val _columnIndexOfLastMessageSenderId: Int = getColumnIndexOrThrow(_stmt, "lastMessageSenderId")
        val _columnIndexOfUnreadCount: Int = getColumnIndexOrThrow(_stmt, "unreadCount")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ConversationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ConversationEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpMatchId: String
          _tmpMatchId = _stmt.getText(_columnIndexOfMatchId)
          val _tmpPeerUserId: String
          _tmpPeerUserId = _stmt.getText(_columnIndexOfPeerUserId)
          val _tmpPeerDisplayName: String
          _tmpPeerDisplayName = _stmt.getText(_columnIndexOfPeerDisplayName)
          val _tmpPeerThumbUrl: String?
          if (_stmt.isNull(_columnIndexOfPeerThumbUrl)) {
            _tmpPeerThumbUrl = null
          } else {
            _tmpPeerThumbUrl = _stmt.getText(_columnIndexOfPeerThumbUrl)
          }
          val _tmpPeerIsOnline: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPeerIsOnline).toInt()
          _tmpPeerIsOnline = _tmp != 0
          val _tmpPeerLastSeenAt: Long?
          if (_stmt.isNull(_columnIndexOfPeerLastSeenAt)) {
            _tmpPeerLastSeenAt = null
          } else {
            _tmpPeerLastSeenAt = _stmt.getLong(_columnIndexOfPeerLastSeenAt)
          }
          val _tmpLastMessagePreview: String?
          if (_stmt.isNull(_columnIndexOfLastMessagePreview)) {
            _tmpLastMessagePreview = null
          } else {
            _tmpLastMessagePreview = _stmt.getText(_columnIndexOfLastMessagePreview)
          }
          val _tmpLastMessageSentAt: Long?
          if (_stmt.isNull(_columnIndexOfLastMessageSentAt)) {
            _tmpLastMessageSentAt = null
          } else {
            _tmpLastMessageSentAt = _stmt.getLong(_columnIndexOfLastMessageSentAt)
          }
          val _tmpLastMessageSenderId: String?
          if (_stmt.isNull(_columnIndexOfLastMessageSenderId)) {
            _tmpLastMessageSenderId = null
          } else {
            _tmpLastMessageSenderId = _stmt.getText(_columnIndexOfLastMessageSenderId)
          }
          val _tmpUnreadCount: Int
          _tmpUnreadCount = _stmt.getLong(_columnIndexOfUnreadCount).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = ConversationEntity(_tmpId,_tmpMatchId,_tmpPeerUserId,_tmpPeerDisplayName,_tmpPeerThumbUrl,_tmpPeerIsOnline,_tmpPeerLastSeenAt,_tmpLastMessagePreview,_tmpLastMessageSentAt,_tmpLastMessageSenderId,_tmpUnreadCount,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun byId(id: String): ConversationEntity? {
    val _sql: String = "SELECT * FROM conversations WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMatchId: Int = getColumnIndexOrThrow(_stmt, "matchId")
        val _columnIndexOfPeerUserId: Int = getColumnIndexOrThrow(_stmt, "peerUserId")
        val _columnIndexOfPeerDisplayName: Int = getColumnIndexOrThrow(_stmt, "peerDisplayName")
        val _columnIndexOfPeerThumbUrl: Int = getColumnIndexOrThrow(_stmt, "peerThumbUrl")
        val _columnIndexOfPeerIsOnline: Int = getColumnIndexOrThrow(_stmt, "peerIsOnline")
        val _columnIndexOfPeerLastSeenAt: Int = getColumnIndexOrThrow(_stmt, "peerLastSeenAt")
        val _columnIndexOfLastMessagePreview: Int = getColumnIndexOrThrow(_stmt, "lastMessagePreview")
        val _columnIndexOfLastMessageSentAt: Int = getColumnIndexOrThrow(_stmt, "lastMessageSentAt")
        val _columnIndexOfLastMessageSenderId: Int = getColumnIndexOrThrow(_stmt, "lastMessageSenderId")
        val _columnIndexOfUnreadCount: Int = getColumnIndexOrThrow(_stmt, "unreadCount")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ConversationEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpMatchId: String
          _tmpMatchId = _stmt.getText(_columnIndexOfMatchId)
          val _tmpPeerUserId: String
          _tmpPeerUserId = _stmt.getText(_columnIndexOfPeerUserId)
          val _tmpPeerDisplayName: String
          _tmpPeerDisplayName = _stmt.getText(_columnIndexOfPeerDisplayName)
          val _tmpPeerThumbUrl: String?
          if (_stmt.isNull(_columnIndexOfPeerThumbUrl)) {
            _tmpPeerThumbUrl = null
          } else {
            _tmpPeerThumbUrl = _stmt.getText(_columnIndexOfPeerThumbUrl)
          }
          val _tmpPeerIsOnline: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPeerIsOnline).toInt()
          _tmpPeerIsOnline = _tmp != 0
          val _tmpPeerLastSeenAt: Long?
          if (_stmt.isNull(_columnIndexOfPeerLastSeenAt)) {
            _tmpPeerLastSeenAt = null
          } else {
            _tmpPeerLastSeenAt = _stmt.getLong(_columnIndexOfPeerLastSeenAt)
          }
          val _tmpLastMessagePreview: String?
          if (_stmt.isNull(_columnIndexOfLastMessagePreview)) {
            _tmpLastMessagePreview = null
          } else {
            _tmpLastMessagePreview = _stmt.getText(_columnIndexOfLastMessagePreview)
          }
          val _tmpLastMessageSentAt: Long?
          if (_stmt.isNull(_columnIndexOfLastMessageSentAt)) {
            _tmpLastMessageSentAt = null
          } else {
            _tmpLastMessageSentAt = _stmt.getLong(_columnIndexOfLastMessageSentAt)
          }
          val _tmpLastMessageSenderId: String?
          if (_stmt.isNull(_columnIndexOfLastMessageSenderId)) {
            _tmpLastMessageSenderId = null
          } else {
            _tmpLastMessageSenderId = _stmt.getText(_columnIndexOfLastMessageSenderId)
          }
          val _tmpUnreadCount: Int
          _tmpUnreadCount = _stmt.getLong(_columnIndexOfUnreadCount).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = ConversationEntity(_tmpId,_tmpMatchId,_tmpPeerUserId,_tmpPeerDisplayName,_tmpPeerThumbUrl,_tmpPeerIsOnline,_tmpPeerLastSeenAt,_tmpLastMessagePreview,_tmpLastMessageSentAt,_tmpLastMessageSenderId,_tmpUnreadCount,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeById(id: String): Flow<ConversationEntity?> {
    val _sql: String = "SELECT * FROM conversations WHERE id = ?"
    return createFlow(__db, false, arrayOf("conversations")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMatchId: Int = getColumnIndexOrThrow(_stmt, "matchId")
        val _columnIndexOfPeerUserId: Int = getColumnIndexOrThrow(_stmt, "peerUserId")
        val _columnIndexOfPeerDisplayName: Int = getColumnIndexOrThrow(_stmt, "peerDisplayName")
        val _columnIndexOfPeerThumbUrl: Int = getColumnIndexOrThrow(_stmt, "peerThumbUrl")
        val _columnIndexOfPeerIsOnline: Int = getColumnIndexOrThrow(_stmt, "peerIsOnline")
        val _columnIndexOfPeerLastSeenAt: Int = getColumnIndexOrThrow(_stmt, "peerLastSeenAt")
        val _columnIndexOfLastMessagePreview: Int = getColumnIndexOrThrow(_stmt, "lastMessagePreview")
        val _columnIndexOfLastMessageSentAt: Int = getColumnIndexOrThrow(_stmt, "lastMessageSentAt")
        val _columnIndexOfLastMessageSenderId: Int = getColumnIndexOrThrow(_stmt, "lastMessageSenderId")
        val _columnIndexOfUnreadCount: Int = getColumnIndexOrThrow(_stmt, "unreadCount")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ConversationEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpMatchId: String
          _tmpMatchId = _stmt.getText(_columnIndexOfMatchId)
          val _tmpPeerUserId: String
          _tmpPeerUserId = _stmt.getText(_columnIndexOfPeerUserId)
          val _tmpPeerDisplayName: String
          _tmpPeerDisplayName = _stmt.getText(_columnIndexOfPeerDisplayName)
          val _tmpPeerThumbUrl: String?
          if (_stmt.isNull(_columnIndexOfPeerThumbUrl)) {
            _tmpPeerThumbUrl = null
          } else {
            _tmpPeerThumbUrl = _stmt.getText(_columnIndexOfPeerThumbUrl)
          }
          val _tmpPeerIsOnline: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPeerIsOnline).toInt()
          _tmpPeerIsOnline = _tmp != 0
          val _tmpPeerLastSeenAt: Long?
          if (_stmt.isNull(_columnIndexOfPeerLastSeenAt)) {
            _tmpPeerLastSeenAt = null
          } else {
            _tmpPeerLastSeenAt = _stmt.getLong(_columnIndexOfPeerLastSeenAt)
          }
          val _tmpLastMessagePreview: String?
          if (_stmt.isNull(_columnIndexOfLastMessagePreview)) {
            _tmpLastMessagePreview = null
          } else {
            _tmpLastMessagePreview = _stmt.getText(_columnIndexOfLastMessagePreview)
          }
          val _tmpLastMessageSentAt: Long?
          if (_stmt.isNull(_columnIndexOfLastMessageSentAt)) {
            _tmpLastMessageSentAt = null
          } else {
            _tmpLastMessageSentAt = _stmt.getLong(_columnIndexOfLastMessageSentAt)
          }
          val _tmpLastMessageSenderId: String?
          if (_stmt.isNull(_columnIndexOfLastMessageSenderId)) {
            _tmpLastMessageSenderId = null
          } else {
            _tmpLastMessageSenderId = _stmt.getText(_columnIndexOfLastMessageSenderId)
          }
          val _tmpUnreadCount: Int
          _tmpUnreadCount = _stmt.getLong(_columnIndexOfUnreadCount).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = ConversationEntity(_tmpId,_tmpMatchId,_tmpPeerUserId,_tmpPeerDisplayName,_tmpPeerThumbUrl,_tmpPeerIsOnline,_tmpPeerLastSeenAt,_tmpLastMessagePreview,_tmpLastMessageSentAt,_tmpLastMessageSenderId,_tmpUnreadCount,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeUnreadTotal(): Flow<Int> {
    val _sql: String = "SELECT COALESCE(SUM(unreadCount), 0) FROM conversations"
    return createFlow(__db, false, arrayOf("conversations")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteNotIn(ids: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM conversations WHERE id NOT IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in ids) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearUnread(id: String) {
    val _sql: String = "UPDATE conversations SET unreadCount = 0 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun incrementUnread(conversationId: String) {
    val _sql: String = "UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, conversationId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePresence(userId: String, online: Boolean) {
    val _sql: String = "UPDATE conversations SET peerIsOnline = ? WHERE peerUserId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (online) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, userId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM conversations"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
