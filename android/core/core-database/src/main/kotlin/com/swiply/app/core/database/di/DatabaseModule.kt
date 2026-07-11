package com.swiply.app.core.database.di

import android.content.Context
import androidx.room.Room
import com.swiply.app.core.database.SwiplyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): SwiplyDatabase =
        Room.databaseBuilder(context, SwiplyDatabase::class.java, "swiply.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun conversationDao(db: SwiplyDatabase) = db.conversationDao()

    @Provides
    fun messageDao(db: SwiplyDatabase) = db.messageDao()

    @Provides
    fun matchDao(db: SwiplyDatabase) = db.matchDao()

    @Provides
    fun myProfileDao(db: SwiplyDatabase) = db.myProfileDao()
}
