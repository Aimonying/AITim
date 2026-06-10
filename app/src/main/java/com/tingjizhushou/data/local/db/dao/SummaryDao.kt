package com.tingjizhushou.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tingjizhushou.data.local.db.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Summary entities.
 * Provides all database operations for meeting summaries.
 */
@Dao
interface SummaryDao {
    
    // ==================== Insert Operations ====================
    
    /**
     * Insert a new summary.
     * @return The auto-generated ID of the inserted summary
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: SummaryEntity): Long
    
    /**
     * Insert multiple summaries.
     * @return List of auto-generated IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(summaries: List<SummaryEntity>): List<Long>
    
    // ==================== Update Operations ====================
    
    /**
     * Update an existing summary.
     */
    @Update
    suspend fun update(summary: SummaryEntity)
    
    /**
     * Update only specific fields of a summary.
     */
    @Query("""
        UPDATE summaries SET 
            title = COALESCE(:title, title),
            location = COALESCE(:location, location),
            participants = COALESCE(:participants, participants),
            agenda = COALESCE(:agenda, agenda),
            conclusion = COALESCE(:conclusion, conclusion),
            rawText = COALESCE(:rawText, rawText),
            updatedAt = :updatedAt
        WHERE id = :summaryId
    """)
    suspend fun updateFields(
        summaryId: Long,
        title: String? = null,
        location: String? = null,
        participants: String? = null,
        agenda: String? = null,
        conclusion: String? = null,
        rawText: String? = null,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    // ==================== Delete Operations ====================
    
    /**
     * Delete a summary by entity.
     */
    @Delete
    suspend fun delete(summary: SummaryEntity)
    
    /**
     * Delete a summary by ID.
     */
    @Query("DELETE FROM summaries WHERE id = :summaryId")
    suspend fun deleteById(summaryId: Long)
    
    /**
     * Delete all summaries for a specific record.
     */
    @Query("DELETE FROM summaries WHERE recordId = :recordId")
    suspend fun deleteByRecordId(recordId: Long)
    
    /**
     * Delete all summaries.
     */
    @Query("DELETE FROM summaries")
    suspend fun deleteAll()
    
    // ==================== Query Operations ====================
    
    /**
     * Get a summary by ID.
     */
    @Query("SELECT * FROM summaries WHERE id = :summaryId")
    suspend fun getById(summaryId: Long): SummaryEntity?
    
    /**
     * Get a summary by ID as Flow for reactive updates.
     */
    @Query("SELECT * FROM summaries WHERE id = :summaryId")
    fun getByIdFlow(summaryId: Long): Flow<SummaryEntity?>
    
    /**
     * Get the summary for a specific record.
     */
    @Query("SELECT * FROM summaries WHERE recordId = :recordId LIMIT 1")
    suspend fun getByRecordId(recordId: Long): SummaryEntity?
    
    /**
     * Get the summary for a specific record as Flow.
     */
    @Query("SELECT * FROM summaries WHERE recordId = :recordId LIMIT 1")
    fun getByRecordIdFlow(recordId: Long): Flow<SummaryEntity?>
    
    /**
     * Get all summaries ordered by creation time (newest first).
     */
    @Query("SELECT * FROM summaries ORDER BY createdAt DESC")
    fun getAllSummaries(): Flow<List<SummaryEntity>>
    
    /**
     * Get recent summaries with limit.
     */
    @Query("SELECT * FROM summaries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int): Flow<List<SummaryEntity>>
    
    /**
     * Search summaries by title, participants, or agenda.
     * Case-insensitive search.
     */
    @Query("""
        SELECT * FROM summaries 
        WHERE title LIKE '%' || :keyword || '%' 
           OR participants LIKE '%' || :keyword || '%'
           OR agenda LIKE '%' || :keyword || '%'
           OR conclusion LIKE '%' || :keyword || '%'
        ORDER BY createdAt DESC
    """)
    fun searchSummaries(keyword: String): Flow<List<SummaryEntity>>
    
    /**
     * Get the count of all summaries.
     */
    @Query("SELECT COUNT(*) FROM summaries")
    suspend fun getSummaryCount(): Int
    
    /**
     * Check if a summary exists for a specific record.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM summaries WHERE recordId = :recordId)")
    suspend fun existsByRecordId(recordId: Long): Boolean
}
