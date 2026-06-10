package com.tingjizhushou.data.repository

import com.tingjizhushou.data.local.db.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Summary operations.
 * Abstracts the data layer from the business logic.
 */
interface SummaryRepository {
    
    /**
     * Insert a new summary.
     * @return The auto-generated ID of the inserted summary
     */
    suspend fun insertSummary(summary: SummaryEntity): Long
    
    /**
     * Insert multiple summaries.
     * @return List of auto-generated IDs
     */
    suspend fun insertSummaries(summaries: List<SummaryEntity>): List<Long>
    
    /**
     * Update an existing summary.
     */
    suspend fun updateSummary(summary: SummaryEntity)
    
    /**
     * Update specific fields of a summary.
     */
    suspend fun updateSummaryFields(
        summaryId: Long,
        title: String? = null,
        location: String? = null,
        participants: String? = null,
        agenda: String? = null,
        conclusion: String? = null,
        rawText: String? = null
    )
    
    /**
     * Delete a summary by entity.
     */
    suspend fun deleteSummary(summary: SummaryEntity)
    
    /**
     * Delete a summary by ID.
     */
    suspend fun deleteSummaryById(summaryId: Long)
    
    /**
     * Delete all summaries for a specific record.
     */
    suspend fun deleteSummariesByRecordId(recordId: Long)
    
    /**
     * Get a summary by ID.
     */
    suspend fun getSummaryById(summaryId: Long): SummaryEntity?
    
    /**
     * Get a summary by ID as Flow.
     */
    fun getSummaryByIdFlow(summaryId: Long): Flow<SummaryEntity?>
    
    /**
     * Get the summary for a specific record.
     */
    suspend fun getSummaryByRecordId(recordId: Long): SummaryEntity?
    
    /**
     * Get the summary for a specific record as Flow.
     */
    fun getSummaryByRecordIdFlow(recordId: Long): Flow<SummaryEntity?>
    
    /**
     * Get all summaries as Flow.
     */
    fun getAllSummaries(): Flow<List<SummaryEntity>>
    
    /**
     * Get recent summaries with limit.
     */
    fun getRecentSummaries(limit: Int): Flow<List<SummaryEntity>>
    
    /**
     * Search summaries by keyword.
     */
    fun searchSummaries(keyword: String): Flow<List<SummaryEntity>>
    
    /**
     * Get total summary count.
     */
    suspend fun getSummaryCount(): Int
    
    /**
     * Check if a summary exists for a specific record.
     */
    suspend fun summaryExistsForRecord(recordId: Long): Boolean
}
