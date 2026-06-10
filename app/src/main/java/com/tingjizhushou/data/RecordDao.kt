package com.tingjizhushou.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 记录数据访问接口
 * 定义对records表的所有数据库操作
 */
@Dao
interface RecordDao {
    
    // 插入新记录
    @Insert
    suspend fun insert(record: RecordEntity): Long
    
    // 更新记录
    @Update
    suspend fun update(record: RecordEntity)
    
    // 删除记录
    @Delete
    suspend fun delete(record: RecordEntity)
    
    // 根据ID获取记录
    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: Long): RecordEntity?
    
    // 获取所有记录（按创建时间倒序）
    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RecordEntity>>
    
    // 根据类型获取记录
    @Query("SELECT * FROM records WHERE type = :type ORDER BY createdAt DESC")
    fun getByType(type: String): Flow<List<RecordEntity>>
    
    // 根据语言获取记录
    @Query("SELECT * FROM records WHERE language = :language ORDER BY createdAt DESC")
    fun getByLanguage(language: String): Flow<List<RecordEntity>>
    
    // 搜索记录（标题、转写文字、纪要）
    @Query("""
        SELECT * FROM records 
        WHERE title LIKE '%' || :keyword || '%' 
           OR transcriptText LIKE '%' || :keyword || '%'
           OR meetingMinutes LIKE '%' || :keyword || '%'
        ORDER BY createdAt DESC
    """)
    fun search(keyword: String): Flow<List<RecordEntity>>
    
    // 获取收藏的记录
    @Query("SELECT * FROM records WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<RecordEntity>>
    
    // 切换收藏状态
    @Query("UPDATE records SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
    
    // 更新转写文字
    @Query("UPDATE records SET transcriptText = :text, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTranscript(id: Long, text: String, updatedAt: java.util.Date)
    
    // 更新会议纪要
    @Query("UPDATE records SET meetingMinutes = :minutes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateMeetingMinutes(id: Long, minutes: String, updatedAt: java.util.Date)
    
    // 更新录音信息
    @Query("UPDATE records SET audioPath = :path, audioSize = :size, duration = :duration WHERE id = :id")
    suspend fun updateAudioInfo(id: Long, path: String, size: Long, duration: Int)
    
    // 删除所有记录
    @Query("DELETE FROM records")
    suspend fun deleteAll()
    
    // 获取记录总数
    @Query("SELECT COUNT(*) FROM records")
    suspend fun getCount(): Int
}
