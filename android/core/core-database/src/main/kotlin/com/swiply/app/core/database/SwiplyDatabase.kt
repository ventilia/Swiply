package com.swiply.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.swiply.app.core.database.dao.ConversationDao
import com.swiply.app.core.database.dao.MatchDao
import com.swiply.app.core.database.dao.MessageDao
import com.swiply.app.core.database.dao.MyProfileDao
import com.swiply.app.core.database.entity.ConversationEntity
import com.swiply.app.core.database.entity.MatchEntity
import com.swiply.app.core.database.entity.MessageEntity
import com.swiply.app.core.database.entity.MyProfileEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MatchEntity::class,
        MyProfileEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SwiplyDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun matchDao(): MatchDao
    abstract fun myProfileDao(): MyProfileDao

    /** Полная очистка при логауте */
    suspend fun clearAll() {
        conversationDao().clear()
        messageDao().clear()
        matchDao().clear()
        myProfileDao().clear()
    }
}
