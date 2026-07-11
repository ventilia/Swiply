package com.swiply.app.core.database.dao

import androidx.paging.PagingSource
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.RoomRawQuery
import androidx.room.paging.LimitOffsetPagingSource
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.swiply.app.core.database.entity.MessageEntity
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MessageDao_Impl(
  __db: RoomDatabase,
) : MessageDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfMessageEntity: EntityUpsertAdapter<MessageEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfMessageEntity = EntityUpsertAdapter<MessageEntity>(object : EntityInsertAdapter<MessageEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `messages` (`id`,`conversationId`,`senderId`,`type`,`content`,`mediaUrl`,`sentAt`,`deliveredAt`,`readAt`,`status`,`clientTempId`,`isPending`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MessageEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.conversationId)
        statement.bindText(3, entity.senderId)
        statement.bindText(4, entity.type)
        val _tmpContent: String? = entity.content
        if (_tmpContent == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpContent)
        }
        val _tmpMediaUrl: String? = entity.mediaUrl
        if (_tmpMediaUrl == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpMediaUrl)
        }
        statement.bindLong(7, entity.sentAt)
        val _tmpDeliveredAt: Long? = entity.deliveredAt
        if (_tmpDeliveredAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpDeliveredAt)
        }
        val _tmpReadAt: Long? = entity.readAt
        if (_tmpReadAt == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpReadAt)
        }
        statement.bindText(10, entity.status)
        val _tmpClientTempId: String? = entity.clientTempId
        if (_tmpClientTempId == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpClientTempId)
        }
        val _tmp: Int = if (entity.isPending) 1 else 0
        statement.bindLong(12, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<MessageEntity>() {
      protected override fun createQuery(): String = "UPDATE `messages` SET `id` = ?,`conversationId` = ?,`senderId` = ?,`type` = ?,`content` = ?,`mediaUrl` = ?,`sentAt` = ?,`deliveredAt` = ?,`readAt` = ?,`status` = ?,`clientTempId` = ?,`isPending` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MessageEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.conversationId)
        statement.bindText(3, entity.senderId)
        statement.bindText(4, entity.type)
        val _tmpContent: String? = entity.content
        if (_tmpContent == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpContent)
        }
        val _tmpMediaUrl: String? = entity.mediaUrl
        if (_tmpMediaUrl == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpMediaUrl)
        }
        statement.bindLong(7, entity.sentAt)
        val _tmpDeliveredAt: Long? = entity.deliveredAt
        if (_tmpDeliveredAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpDeliveredAt)
        }
        val _tmpReadAt: Long? = entity.readAt
        if (_tmpReadAt == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpReadAt)
        }
        statement.bindText(10, entity.status)
        val _tmpClientTempId: String? = entity.clientTempId
        if (_tmpClientTempId == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpClientTempId)
        }
        val _tmp: Int = if (entity.isPending) 1 else 0
        statement.bindLong(12, _tmp.toLong())
        statement.bindText(13, entity.id)
      }
    })
  }

  public override suspend fun upsert(item: MessageEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfMessageEntity.upsert(_connection, item)
  }

  public override suspend fun upsertAll(items: List<MessageEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfMessageEntity.upsert(_connection, items)
  }

  public override fun pagingSource(conversationId: String): PagingSource<Int, MessageEntity> {
    val _sql: String = "SELECT * FROM messages WHERE conversationId = ? ORDER BY sentAt DESC, id DESC"
    val _rawQuery: RoomRawQuery = RoomRawQuery(_sql) { _stmt ->
      var _argIndex: Int = 1
      _stmt.bindText(_argIndex, conversationId)
    }
    return object : LimitOffsetPagingSource<MessageEntity>(_rawQuery, __db, "messages") {
      protected override suspend fun convertRows(limitOffsetQuery: RoomRawQuery, itemCount: Int): List<MessageEntity> = performSuspending(__db, true, false) { _connection ->
        val _stmt: SQLiteStatement = _connection.prepare(limitOffsetQuery.sql)
        limitOffsetQuery.getBindingFunction().invoke(_stmt)
        try {
          val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
          val _columnIndexOfConversationId: Int = getColumnIndexOrThrow(_stmt, "conversationId")
          val _columnIndexOfSenderId: Int = getColumnIndexOrThrow(_stmt, "senderId")
          val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
          val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
          val _columnIndexOfMediaUrl: Int = getColumnIndexOrThrow(_stmt, "mediaUrl")
          val _columnIndexOfSentAt: Int = getColumnIndexOrThrow(_stmt, "sentAt")
          val _columnIndexOfDeliveredAt: Int = getColumnIndexOrThrow(_stmt, "deliveredAt")
          val _columnIndexOfReadAt: Int = getColumnIndexOrThrow(_stmt, "readAt")
          val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
          val _columnIndexOfClientTempId: Int = getColumnIndexOrThrow(_stmt, "clientTempId")
          val _columnIndexOfIsPending: Int = getColumnIndexOrThrow(_stmt, "isPending")
          val _result: MutableList<MessageEntity> = mutableListOf()
          while (_stmt.step()) {
            val _item: MessageEntity
            val _tmpId: String
            _tmpId = _stmt.getText(_columnIndexOfId)
            val _tmpConversationId: String
            _tmpConversationId = _stmt.getText(_columnIndexOfConversationId)
            val _tmpSenderId: String
            _tmpSenderId = _stmt.getText(_columnIndexOfSenderId)
            val _tmpType: String
            _tmpType = _stmt.getText(_columnIndexOfType)
            val _tmpContent: String?
            if (_stmt.isNull(_columnIndexOfContent)) {
              _tmpContent = null
            } else {
              _tmpContent = _stmt.getText(_columnIndexOfContent)
            }
            val _tmpMediaUrl: String?
            if (_stmt.isNull(_columnIndexOfMediaUrl)) {
              _tmpMediaUrl = null
            } else {
              _tmpMediaUrl = _stmt.getText(_columnIndexOfMediaUrl)
            }
            val _tmpSentAt: Long
            _tmpSentAt = _stmt.getLong(_columnIndexOfSentAt)
            val _tmpDeliveredAt: Long?
            if (_stmt.isNull(_columnIndexOfDeliveredAt)) {
              _tmpDeliveredAt = null
            } else {
              _tmpDeliveredAt = _stmt.getLong(_columnIndexOfDeliveredAt)
            }
            val _tmpReadAt: Long?
            if (_stmt.isNull(_columnIndexOfReadAt)) {
              _tmpReadAt = null
            } else {
              _tmpReadAt = _stmt.getLong(_columnIndexOfReadAt)
            }
            val _tmpStatus: String
            _tmpStatus = _stmt.getText(_columnIndexOfStatus)
            val _tmpClientTempId: String?
            if (_stmt.isNull(_columnIndexOfClientTempId)) {
              _tmpClientTempId = null
            } else {
              _tmpClientTempId = _stmt.getText(_columnIndexOfClientTempId)
            }
            val _tmpIsPending: Boolean
            val _tmp: Int
            _tmp = _stmt.getLong(_columnIndexOfIsPending).toInt()
            _tmpIsPending = _tmp != 0
            _item = MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpType,_tmpContent,_tmpMediaUrl,_tmpSentAt,_tmpDeliveredAt,_tmpReadAt,_tmpStatus,_tmpClientTempId,_tmpIsPending)
            _result.add(_item)
          }
          _result
        } finally {
          _stmt.close()
        }
      }
    }
  }

  public override suspend fun pendingMessages(): List<MessageEntity> {
    val _sql: String = "SELECT * FROM messages WHERE isPending = 1 ORDER BY sentAt"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfConversationId: Int = getColumnIndexOrThrow(_stmt, "conversationId")
        val _columnIndexOfSenderId: Int = getColumnIndexOrThrow(_stmt, "senderId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfMediaUrl: Int = getColumnIndexOrThrow(_stmt, "mediaUrl")
        val _columnIndexOfSentAt: Int = getColumnIndexOrThrow(_stmt, "sentAt")
        val _columnIndexOfDeliveredAt: Int = getColumnIndexOrThrow(_stmt, "deliveredAt")
        val _columnIndexOfReadAt: Int = getColumnIndexOrThrow(_stmt, "readAt")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfClientTempId: Int = getColumnIndexOrThrow(_stmt, "clientTempId")
        val _columnIndexOfIsPending: Int = getColumnIndexOrThrow(_stmt, "isPending")
        val _result: MutableList<MessageEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MessageEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpConversationId: String
          _tmpConversationId = _stmt.getText(_columnIndexOfConversationId)
          val _tmpSenderId: String
          _tmpSenderId = _stmt.getText(_columnIndexOfSenderId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpContent: String?
          if (_stmt.isNull(_columnIndexOfContent)) {
            _tmpContent = null
          } else {
            _tmpContent = _stmt.getText(_columnIndexOfContent)
          }
          val _tmpMediaUrl: String?
          if (_stmt.isNull(_columnIndexOfMediaUrl)) {
            _tmpMediaUrl = null
          } else {
            _tmpMediaUrl = _stmt.getText(_columnIndexOfMediaUrl)
          }
          val _tmpSentAt: Long
          _tmpSentAt = _stmt.getLong(_columnIndexOfSentAt)
          val _tmpDeliveredAt: Long?
          if (_stmt.isNull(_columnIndexOfDeliveredAt)) {
            _tmpDeliveredAt = null
          } else {
            _tmpDeliveredAt = _stmt.getLong(_columnIndexOfDeliveredAt)
          }
          val _tmpReadAt: Long?
          if (_stmt.isNull(_columnIndexOfReadAt)) {
            _tmpReadAt = null
          } else {
            _tmpReadAt = _stmt.getLong(_columnIndexOfReadAt)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpClientTempId: String?
          if (_stmt.isNull(_columnIndexOfClientTempId)) {
            _tmpClientTempId = null
          } else {
            _tmpClientTempId = _stmt.getText(_columnIndexOfClientTempId)
          }
          val _tmpIsPending: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPending).toInt()
          _tmpIsPending = _tmp != 0
          _item = MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpType,_tmpContent,_tmpMediaUrl,_tmpSentAt,_tmpDeliveredAt,_tmpReadAt,_tmpStatus,_tmpClientTempId,_tmpIsPending)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePendingByTempId(tempId: String) {
    val _sql: String = "DELETE FROM messages WHERE clientTempId = ? AND isPending = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, tempId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markMyMessagesRead(
    conversationId: String,
    myUserId: String,
    readAtMillis: Long,
  ) {
    val _sql: String = "UPDATE messages SET readAt = ?, status = 'READ' WHERE conversationId = ? AND senderId = ? AND readAt IS NULL"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, readAtMillis)
        _argIndex = 2
        _stmt.bindText(_argIndex, conversationId)
        _argIndex = 3
        _stmt.bindText(_argIndex, myUserId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearConversation(conversationId: String) {
    val _sql: String = "DELETE FROM messages WHERE conversationId = ?"
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

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM messages"
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
