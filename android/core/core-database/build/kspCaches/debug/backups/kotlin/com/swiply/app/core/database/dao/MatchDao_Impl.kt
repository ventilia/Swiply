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
import com.swiply.app.core.database.entity.MatchEntity
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
public class MatchDao_Impl(
  __db: RoomDatabase,
) : MatchDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfMatchEntity: EntityUpsertAdapter<MatchEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfMatchEntity = EntityUpsertAdapter<MatchEntity>(object : EntityInsertAdapter<MatchEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `matches` (`matchId`,`userId`,`displayName`,`age`,`thumbUrl`,`isOnline`,`matchedAt`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MatchEntity) {
        statement.bindText(1, entity.matchId)
        statement.bindText(2, entity.userId)
        statement.bindText(3, entity.displayName)
        statement.bindLong(4, entity.age.toLong())
        val _tmpThumbUrl: String? = entity.thumbUrl
        if (_tmpThumbUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpThumbUrl)
        }
        val _tmp: Int = if (entity.isOnline) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.matchedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<MatchEntity>() {
      protected override fun createQuery(): String = "UPDATE `matches` SET `matchId` = ?,`userId` = ?,`displayName` = ?,`age` = ?,`thumbUrl` = ?,`isOnline` = ?,`matchedAt` = ? WHERE `matchId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MatchEntity) {
        statement.bindText(1, entity.matchId)
        statement.bindText(2, entity.userId)
        statement.bindText(3, entity.displayName)
        statement.bindLong(4, entity.age.toLong())
        val _tmpThumbUrl: String? = entity.thumbUrl
        if (_tmpThumbUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpThumbUrl)
        }
        val _tmp: Int = if (entity.isOnline) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.matchedAt)
        statement.bindText(8, entity.matchId)
      }
    })
  }

  public override suspend fun upsertAll(items: List<MatchEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfMatchEntity.upsert(_connection, items)
  }

  public override fun observeAll(): Flow<List<MatchEntity>> {
    val _sql: String = "SELECT * FROM matches ORDER BY matchedAt DESC"
    return createFlow(__db, false, arrayOf("matches")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfMatchId: Int = getColumnIndexOrThrow(_stmt, "matchId")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfDisplayName: Int = getColumnIndexOrThrow(_stmt, "displayName")
        val _columnIndexOfAge: Int = getColumnIndexOrThrow(_stmt, "age")
        val _columnIndexOfThumbUrl: Int = getColumnIndexOrThrow(_stmt, "thumbUrl")
        val _columnIndexOfIsOnline: Int = getColumnIndexOrThrow(_stmt, "isOnline")
        val _columnIndexOfMatchedAt: Int = getColumnIndexOrThrow(_stmt, "matchedAt")
        val _result: MutableList<MatchEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MatchEntity
          val _tmpMatchId: String
          _tmpMatchId = _stmt.getText(_columnIndexOfMatchId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpDisplayName: String
          _tmpDisplayName = _stmt.getText(_columnIndexOfDisplayName)
          val _tmpAge: Int
          _tmpAge = _stmt.getLong(_columnIndexOfAge).toInt()
          val _tmpThumbUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbUrl)) {
            _tmpThumbUrl = null
          } else {
            _tmpThumbUrl = _stmt.getText(_columnIndexOfThumbUrl)
          }
          val _tmpIsOnline: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOnline).toInt()
          _tmpIsOnline = _tmp != 0
          val _tmpMatchedAt: Long
          _tmpMatchedAt = _stmt.getLong(_columnIndexOfMatchedAt)
          _item = MatchEntity(_tmpMatchId,_tmpUserId,_tmpDisplayName,_tmpAge,_tmpThumbUrl,_tmpIsOnline,_tmpMatchedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteNotIn(ids: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM matches WHERE matchId NOT IN (")
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

  public override suspend fun delete(matchId: String) {
    val _sql: String = "DELETE FROM matches WHERE matchId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, matchId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePresence(userId: String, online: Boolean) {
    val _sql: String = "UPDATE matches SET isOnline = ? WHERE userId = ?"
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
    val _sql: String = "DELETE FROM matches"
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
