package com.tingjizhushou.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tingjizhushou.data.local.db.entity.TranscriptEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Transcript entities.
 * Provides all database operations for transcript segments.
 */
@Dao
interface TranscriptDao {
    
    // ==================== Insert Operations ====================
    
    /**
     * Insert a new transcript.
     * @return The auto-generated ID of the inserted transcript
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transcript: TranscriptEntity): Long
    
    /**
     * Insert multiple transcripts.
     * @return List of auto-generated IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transcripts: List<TranscriptEntity>): List<Long>
    
    // ==================== Update Operations ====================
    
    /**
     * Update an existing transcript.
     */
    @Update
    suspend fun update(transcript: TranscriptEntity)
    
    // ==================== Delete Operations ====================
    
    /**
     * Delete a transcript by entity.
     */
    @Delete
    suspend fun delete(transcript: TranscriptEntity)
    
    /**
     * Delete a transcript by ID.
     */
    @Query("DELETE FROM transcripts WHERE id = :transcriptId")
    suspend fun deleteById(transcriptId: Long)
    
    /**
     * Delete all transcripts for a specific record.
     */
    @Query("DELETE FROM transcripts WHERE recordId = :recordId")
    suspend fun deleteByRecordId(recordId: Long)
    
    /**
     * Delete all transcripts.
     */
    @Query("DELETE FROM transcripts")
    suspend fun deleteAll()
    
    // ==================== Query Operations ====================
    
    /**
     * Get a transcript by ID.
     */
    @Query("SELECT * FROM transcripts WHERE id = :transcriptId")
    suspend fun getById(transcriptId: Long): TranscriptEntity?
    
    /**
     * Get all transcripts for a specific record.
     * Ordered by creation time.
     */
    @Query("SELECT * FROM transcripts WHERE recordId = :recordId ORDER BY createdAt ASC")
    fun getByRecordId(recordId: Long): Flow<List<TranscriptEntity>>
    
    /**
     * Get all transcripts for a specific record (non-Flow version).
     */
    @Query("SELECT * FROM transcripts WHERE recordId = :recordId ORDER BY createdAt ASC")
    suspend fun getByRecordIdSync(recordId: Long): List<TranscriptEntity>
    
    /**
     * Get the latest transcript for a specific record.
     */
    @Query("SELECT * FROM transcripts WHERE recordId = :recordId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByRecordId(recordId: Long): TranscriptEntity?
    
    /**
     * Get the count of transcripts for a specific record.
     */
    @Query("SELECT COUNT(*) FROM transcripts WHERE recordId = :recordId")
    suspend fun getCountByRecordId(recordId: Long): Int
    
    /**
     * Get all transcripts.
     */
    @Query("SELECT * FROM transcripts ORDER BY createdAt DESC")
    fun getAllTranscripts(): Flow<List<TranscriptEntity>>
    
    /**
     * Get transcripts by engine type.
     */
    @Query("SELECT * FROM transcripts WHERE engine = :engine ORDER BY createdAt DESC")
    fun getTranscriptsByEngine(engine: String): Flow<List<TranscriptEntity>>
}
