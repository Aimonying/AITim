package com.tingjizhushou.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tingjizhushou.data.dao.SubscriptionDao
import com.tingjizhushou.data.dao.UserDao
import com.tingjizhushou.data.local.db.dao.RecordDao
import com.tingjizhushou.data.local.db.dao.SummaryDao
import com.tingjizhushou.data.local.db.dao.TranscriptDao
import com.tingjizhushou.data.local.db.entity.RecordEntity
import com.tingjizhushou.data.local.db.entity.SummaryEntity
import com.tingjizhushou.data.local.db.entity.TranscriptEntity
import com.tingjizhushou.data.model.SubscriptionStatus
import com.tingjizhushou.data.model.UserEntity

/**
 * Room database for TingJiZhuShou app.
 * Contains all entities: Record, Transcript, Summary, and SubscriptionStatus.
 */
@Database(
    entities = [
        RecordEntity::class,
        TranscriptEntity::class,
        SummaryEntity::class,
        SubscriptionStatus::class,
        UserEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Get the RecordDao for accessing record data.
     */
    abstract fun recordDao(): RecordDao
    
    /**
     * Get the TranscriptDao for accessing transcript data.
     */
    abstract fun transcriptDao(): TranscriptDao
    
    /**
     * Get the SummaryDao for accessing summary data.
     */
    abstract fun summaryDao(): SummaryDao
    
    /**
     * Get the SubscriptionDao for accessing subscription data.
     */
    abstract fun subscriptionDao(): SubscriptionDao
    
    abstract fun userDao(): UserDao
    
    companion object {
        /**
         * Database name constant.
         */
        const val DATABASE_NAME = "tingjizhushou_database"
    }
}