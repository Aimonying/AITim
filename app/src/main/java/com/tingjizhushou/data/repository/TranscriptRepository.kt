package com.tingjizhushou.data.repository

import com.tingjizhushou.data.local.db.entity.TranscriptEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Transcript operations.
 * Abstracts the data layer from the business logic.
 */
interface TranscriptRepository {
    
    /**
     * Insert a new transcript.
     * @return The auto-generated ID of the inserted transcript
     */
    suspend fun insertTranscript(transcript: TranscriptEntity): Long
    
    /**
     * Insert multiple transcripts.
     * @return List of auto-generated IDs
     */
    suspend fun insertTranscripts(transcripts: List<TranscriptEntity>): List<Long>
    
    /**
     * Update an existing transcript.
     */
    suspend fun updateTranscript(transcript: TranscriptEntity)
    
    /**
     * Delete a transcript by entity.
     */
    suspend fun deleteTranscript(transcript: TranscriptEntity)
    
    /**
     * Delete a transcript by ID.
     */
    suspend fun deleteTranscriptById(transcriptId: Long)
    
    /**
     * Delete all transcripts for a specific record.
     */
    suspend fun deleteTranscriptsByRecordId(recordId: Long)
    
    /**
     * Get a transcript by ID.
     */
    suspend fun getTranscriptById(transcriptId: Long): TranscriptEntity?
    
    /**
     * Get all transcripts for a specific record as Flow.
     */
    fun getTranscriptsByRecordId(recordId: Long): Flow<List<TranscriptEntity>>
    
    /**
     * Get all transcripts for a specific record (synchronous).
     */
    suspend fun getTranscriptsByRecordIdSync(recordId: Long): List<TranscriptEntity>
    
    /**
     * Get the latest transcript for a specific record.
     */
    suspend fun getLatestTranscript(recordId: Long): TranscriptEntity?
    
    /**
     * Get all transcripts as Flow.
     */
    fun getAllTranscripts(): Flow<List<TranscriptEntity>>
    
    /**
     * Get transcripts by engine type.
     */
    fun getTranscriptsByEngine(engine: String): Flow<List<TranscriptEntity>>
    
    /**
     * Get transcript count for a specific record.
     */
    suspend fun getTranscriptCount(recordId: Long): Int
}
