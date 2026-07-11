package com.swiply.app.core.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.swiply.app.core.database.dao.ConversationDao
import com.swiply.app.core.database.dao.ConversationDao_Impl
import com.swiply.app.core.database.dao.MatchDao
import com.swiply.app.core.database.dao.MatchDao_Impl
import com.swiply.app.core.database.dao.MessageDao
import com.swiply.app.core.database.dao.MessageDao_Impl
import com.swiply.app.core.database.dao.MyProfileDao
import com.swiply.app.core.database.dao.MyProfileDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SwiplyDatabase_Impl : SwiplyDatabase() {
  private val _conversationDao: Lazy<ConversationDao> = lazy {
    ConversationDao_Impl(this)
  }

  private val _messageDao: Lazy<MessageDao> = lazy {
    MessageDao_Impl(this)
  }

  private val _matchDao: Lazy<MatchDao> = lazy {
    MatchDao_Impl(this)
  }

  private val _myProfileDao: Lazy<MyProfileDao> = lazy {
    MyProfileDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "53c08f356a86dda26c947b97a57a59c5", "dcc5bc8472cfc54b275bb4b8b0e4d5e1") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `conversations` (`id` TEXT NOT NULL, `matchId` TEXT NOT NULL, `peerUserId` TEXT NOT NULL, `peerDisplayName` TEXT NOT NULL, `peerThumbUrl` TEXT, `peerIsOnline` INTEGER NOT NULL, `peerLastSeenAt` INTEGER, `lastMessagePreview` TEXT, `lastMessageSentAt` INTEGER, `lastMessageSenderId` TEXT, `unreadCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `type` TEXT NOT NULL, `content` TEXT, `mediaUrl` TEXT, `sentAt` INTEGER NOT NULL, `deliveredAt` INTEGER, `readAt` INTEGER, `status` TEXT NOT NULL, `clientTempId` TEXT, `isPending` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversationId_sentAt` ON `messages` (`conversationId`, `sentAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `matches` (`matchId` TEXT NOT NULL, `userId` TEXT NOT NULL, `displayName` TEXT NOT NULL, `age` INTEGER NOT NULL, `thumbUrl` TEXT, `isOnline` INTEGER NOT NULL, `matchedAt` INTEGER NOT NULL, PRIMARY KEY(`matchId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `my_profile` (`id` INTEGER NOT NULL, `json` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '53c08f356a86dda26c947b97a57a59c5')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `conversations`")
        connection.execSQL("DROP TABLE IF EXISTS `messages`")
        connection.execSQL("DROP TABLE IF EXISTS `matches`")
        connection.execSQL("DROP TABLE IF EXISTS `my_profile`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsConversations: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsConversations.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("matchId", TableInfo.Column("matchId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("peerUserId", TableInfo.Column("peerUserId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("peerDisplayName", TableInfo.Column("peerDisplayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("peerThumbUrl", TableInfo.Column("peerThumbUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("peerIsOnline", TableInfo.Column("peerIsOnline", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("peerLastSeenAt", TableInfo.Column("peerLastSeenAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("lastMessagePreview", TableInfo.Column("lastMessagePreview", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("lastMessageSentAt", TableInfo.Column("lastMessageSentAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("lastMessageSenderId", TableInfo.Column("lastMessageSenderId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("unreadCount", TableInfo.Column("unreadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysConversations: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesConversations: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoConversations: TableInfo = TableInfo("conversations", _columnsConversations, _foreignKeysConversations, _indicesConversations)
        val _existingConversations: TableInfo = read(connection, "conversations")
        if (!_infoConversations.equals(_existingConversations)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |conversations(com.swiply.app.core.database.entity.ConversationEntity).
              | Expected:
              |""".trimMargin() + _infoConversations + """
              |
              | Found:
              |""".trimMargin() + _existingConversations)
        }
        val _columnsMessages: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMessages.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("conversationId", TableInfo.Column("conversationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("senderId", TableInfo.Column("senderId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("content", TableInfo.Column("content", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("mediaUrl", TableInfo.Column("mediaUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("sentAt", TableInfo.Column("sentAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("deliveredAt", TableInfo.Column("deliveredAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("readAt", TableInfo.Column("readAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("clientTempId", TableInfo.Column("clientTempId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("isPending", TableInfo.Column("isPending", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMessages: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMessages: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesMessages.add(TableInfo.Index("index_messages_conversationId_sentAt", false, listOf("conversationId", "sentAt"), listOf("ASC", "ASC")))
        val _infoMessages: TableInfo = TableInfo("messages", _columnsMessages, _foreignKeysMessages, _indicesMessages)
        val _existingMessages: TableInfo = read(connection, "messages")
        if (!_infoMessages.equals(_existingMessages)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |messages(com.swiply.app.core.database.entity.MessageEntity).
              | Expected:
              |""".trimMargin() + _infoMessages + """
              |
              | Found:
              |""".trimMargin() + _existingMessages)
        }
        val _columnsMatches: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMatches.put("matchId", TableInfo.Column("matchId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatches.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatches.put("displayName", TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatches.put("age", TableInfo.Column("age", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatches.put("thumbUrl", TableInfo.Column("thumbUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatches.put("isOnline", TableInfo.Column("isOnline", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatches.put("matchedAt", TableInfo.Column("matchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMatches: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMatches: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMatches: TableInfo = TableInfo("matches", _columnsMatches, _foreignKeysMatches, _indicesMatches)
        val _existingMatches: TableInfo = read(connection, "matches")
        if (!_infoMatches.equals(_existingMatches)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |matches(com.swiply.app.core.database.entity.MatchEntity).
              | Expected:
              |""".trimMargin() + _infoMatches + """
              |
              | Found:
              |""".trimMargin() + _existingMatches)
        }
        val _columnsMyProfile: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMyProfile.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMyProfile.put("json", TableInfo.Column("json", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMyProfile: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMyProfile: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMyProfile: TableInfo = TableInfo("my_profile", _columnsMyProfile, _foreignKeysMyProfile, _indicesMyProfile)
        val _existingMyProfile: TableInfo = read(connection, "my_profile")
        if (!_infoMyProfile.equals(_existingMyProfile)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |my_profile(com.swiply.app.core.database.entity.MyProfileEntity).
              | Expected:
              |""".trimMargin() + _infoMyProfile + """
              |
              | Found:
              |""".trimMargin() + _existingMyProfile)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "conversations", "messages", "matches", "my_profile")
  }

  public override fun clearAllTables() {
    super.performClear(false, "conversations", "messages", "matches", "my_profile")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ConversationDao::class, ConversationDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MessageDao::class, MessageDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MatchDao::class, MatchDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MyProfileDao::class, MyProfileDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun conversationDao(): ConversationDao = _conversationDao.value

  public override fun messageDao(): MessageDao = _messageDao.value

  public override fun matchDao(): MatchDao = _matchDao.value

  public override fun myProfileDao(): MyProfileDao = _myProfileDao.value
}
