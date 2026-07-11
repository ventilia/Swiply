package com.swiply.app.core.database.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.swiply.app.core.database.entity.MyProfileEntity
import javax.`annotation`.processing.Generated
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MyProfileDao_Impl(
  __db: RoomDatabase,
) : MyProfileDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfMyProfileEntity: EntityUpsertAdapter<MyProfileEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfMyProfileEntity = EntityUpsertAdapter<MyProfileEntity>(object : EntityInsertAdapter<MyProfileEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `my_profile` (`id`,`json`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MyProfileEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.json)
      }
    }, object : EntityDeleteOrUpdateAdapter<MyProfileEntity>() {
      protected override fun createQuery(): String = "UPDATE `my_profile` SET `id` = ?,`json` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MyProfileEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.json)
        statement.bindLong(3, entity.id.toLong())
      }
    })
  }

  public override suspend fun upsert(entity: MyProfileEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfMyProfileEntity.upsert(_connection, entity)
  }

  public override fun observe(): Flow<String?> {
    val _sql: String = "SELECT json FROM my_profile WHERE id = 1"
    return createFlow(__db, false, arrayOf("my_profile")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: String?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getText(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun `get`(): String? {
    val _sql: String = "SELECT json FROM my_profile WHERE id = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: String?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getText(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear() {
    val _sql: String = "DELETE FROM my_profile"
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
