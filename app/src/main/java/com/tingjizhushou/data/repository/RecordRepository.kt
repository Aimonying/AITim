package com.tingjizhushou.data.repository

import com.tingjizhushou.data.local.db.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Record operations.
 * Abstracts the data layer from the business logic.
 */
interface RecordRepository {
    
    /**
     * Insert a new record.
     * @return The auto-generated ID of the inserted record
     */
    suspend fun insertRecord(record: RecordEntity): Long
    
    /**
     * Update an existing record.
     */
    suspend fun updateRecord(record: RecordEntity)
    
    /**
     * Delete a record by entity.
     */
    suspend fun deleteRecord(record: RecordEntity)
    
    /**
     * Delete a record by ID.
     */
    suspend fun deleteRecordById(recordId: Long)
    
    /**
     * Get a record by ID.
     */
    suspend fun getRecordById(recordId: Long): RecordEntity?
    
    /**
     * Get a record by ID as Flow.
     */
    fun getRecordByIdFlow(recordId: Long): Flow<RecordEntity?>
    
    /**
     * Get all records as Flow.
     */
    fun getAllRecords(): Flow<List<RecordEntity>>
    
    /**
     * Get records by type.
     */
    fun getRecordsByType(type: String): Flow<List<RecordEntity>>
    
    /**
     * Get records by language.
     */
    fun getRecordsByLanguage(language: String): Flow<List<RecordEntity>>
    
    /**
     * Search records by keyword.
     */
    fun searchRecords(keyword: String): Flow<List<RecordEntity>>
    
    /**
     * Get all favorite records.
     */
    fun getFavoriteRecords(): Flow<List<RecordEntity>>
    
    /**
     * Toggle favorite status for a record.
     */
    suspend fun toggleFavorite(recordId: Long, isFavorite: Boolean)
    
    /**
     * Update transcript text for a record.
     */
    suspend fun updateTranscript(recordId: Long, transcriptText: String)
    
    /**
     * Update meeting minutes for a record.
     */
    suspend fun updateMeetingMinutes(recordId: Long, meetingMinutes: String)
    
    /**
     * Get recent records with limit.
     */
    fun getRecentRecords(limit: Int): Flow<List<RecordEntity>>
    
    /**
     * Get total record count.
     */
    suspend fun getRecordCount(): Int
}
