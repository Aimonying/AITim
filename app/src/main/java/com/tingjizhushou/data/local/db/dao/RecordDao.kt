package com.tingjizhushou.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tingjizhushou.data.local.db.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Record entities.
 * Provides all database operations for recording records.
 */
@Dao
interface RecordDao {
    
    // ==================== Insert Operations ====================
    
    /**
     * Insert a new record.
     * @return The auto-generated ID of the inserted record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordEntity): Long
    
    /**
     * Insert multiple records.
     * @return List of auto-generated IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<RecordEntity>): List<Long>
    
    // ==================== Update Operations ====================
    
    /**
     * Update an existing record.
     */
    @Update
    suspend fun update(record: RecordEntity)
    
    /**
     * Update only the transcript text of a record.
     */
    @Query("UPDATE records SET transcriptText = :transcriptText, updatedAt = :updatedAt WHERE id = :recordId")
    suspend fun updateTranscript(recordId: Long, transcriptText: String, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Update meeting minutes of a record.
     */
    @Query("UPDATE records SET meetingMinutes = :meetingMinutes, updatedAt = :updatedAt WHERE id = :recordId")
    suspend fun updateMeetingMinutes(recordId: Long, meetingMinutes: String, updatedAt: Long = System.currentTimeMillis())
    
    // ==================== Delete Operations ====================
    
    /**
     * Delete a record by entity.
     */
    @Delete
    suspend fun delete(record: RecordEntity)
    
    /**
     * Delete a record by ID.
     */
    @Query("DELETE FROM records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)
    
    /**
     * Delete all records.
     */
    @Query("DELETE FROM records")
    suspend fun deleteAll()
    
    // ==================== Query Operations ====================
    
    /**
     * Get a record by ID.
     */
    @Query("SELECT * FROM records WHERE id = :recordId")
    suspend fun getById(recordId: Long): RecordEntity?
    
    /**
     * Get a record by ID as Flow for reactive updates.
     */
    @Query("SELECT * FROM records WHERE id = :recordId")
    fun getByIdFlow(recordId: Long): Flow<RecordEntity?>
    
    /**
     * Get all records ordered by creation time (newest first).
     */
    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<RecordEntity>>
    
    /**
     * Get records by type (REALTIME, UPLOAD, TEXT).
     */
    @Query("SELECT * FROM records WHERE type = :type ORDER BY createdAt DESC")
    fun getRecordsByType(type: String): Flow<List<RecordEntity>>
    
    /**
     * Get records by language.
     */
    @Query("SELECT * FROM records WHERE language = :language ORDER BY createdAt DESC")
    fun getRecordsByLanguage(language: String): Flow<List<RecordEntity>>
    
    /**
     * Search records by title or transcript content.
     * Case-insensitive search.
     */
    @Query("""
        SELECT * FROM records 
        WHERE title LIKE '%' || :keyword || '%' 
           OR transcriptText LIKE '%' || :keyword || '%'
           OR tags LIKE '%' || :keyword || '%'
        ORDER BY createdAt DESC
    """)
    fun searchRecords(keyword: String): Flow<List<RecordEntity>>
    
    /**
     * Get all favorite records.
     */
    @Query("SELECT * FROM records WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRecords(): Flow<List<RecordEntity>>
    
    /**
     * Set favorite status for a record.
     */
    @Query("UPDATE records SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :recordId")
    suspend fun setFavorite(recordId: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Get the count of all records.
     */
    @Query("SELECT COUNT(*) FROM records")
    suspend fun getRecordCount(): Int
    
    /**
     * Get recent records with limit.
     */
    @Query("SELECT * FROM records ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<RecordEntity>>
}
