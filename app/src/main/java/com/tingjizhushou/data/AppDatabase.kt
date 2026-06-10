package com.tingjizhushou.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Room数据库类
 * 版本：1
 * 包含表：records
 */
@Database(entities = [RecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    // 获取RecordDao
    abstract fun recordDao(): RecordDao
    
    companion object {
        // 单例模式
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tingjizhushou_database"
                )
                    .fallbackToDestructiveMigration() // 数据库升级时销毁旧数据（开发阶段）
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 数据库创建时初始化一些数据（可选）
                        }
                    })
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
