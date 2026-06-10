package com.tingjizhushou.di

import android.app.Application
import androidx.room.Room
import com.tingjizhushou.data.local.db.AppDatabase
import com.tingjizhushou.data.local.db.dao.RecordDao
import com.tingjizhushou.data.local.db.dao.SummaryDao
import com.tingjizhushou.data.local.db.dao.TranscriptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies.
 * Provides singleton instances of AppDatabase and all DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provide the Room database instance.
     * Uses the application context and creates/opens the database lazily.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Application
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    /**
     * Provide RecordDao from the database.
     */
    @Provides
    @Singleton
    fun provideRecordDao(database: AppDatabase): RecordDao {
        return database.recordDao()
    }
    
    /**
     * Provide TranscriptDao from the database.
     */
    @Provides
    @Singleton
    fun provideTranscriptDao(database: AppDatabase): TranscriptDao {
        return database.transcriptDao()
    }
    
    /**
     * Provide SummaryDao from the database.
     */
    @Provides
    @Singleton
    fun provideSummaryDao(database: AppDatabase): SummaryDao {
        return database.summaryDao()
    }
}
