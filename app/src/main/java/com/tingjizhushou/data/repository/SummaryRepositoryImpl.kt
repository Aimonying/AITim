package com.tingjizhushou.data.repository

import com.tingjizhushou.data.local.db.dao.SummaryDao
import com.tingjizhushou.data.local.db.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SummaryRepository.
 * Provides concrete implementation of summary operations using Room DAO.
 */
@Singleton
class SummaryRepositoryImpl @Inject constructor(
    private val summaryDao: SummaryDao
) : SummaryRepository {
    
    override suspend fun insertSummary(summary: SummaryEntity): Long {
        return summaryDao.insert(summary)
    }
    
    override suspend fun insertSummaries(summaries: List<SummaryEntity>): List<Long> {
        return summaryDao.insertAll(summaries)
    }
    
    override suspend fun updateSummary(summary: SummaryEntity) {
        summaryDao.update(summary)
    }
    
    override suspend fun updateSummaryFields(
        summaryId: Long,
        title: String?,
        location: String?,
        participants: String?,
        agenda: String?,
        conclusion: String?,
        rawText: String?
    ) {
        summaryDao.updateFields(
            summaryId = summaryId,
            title = title,
            location = location,
            participants = participants,
            agenda = agenda,
            conclusion = conclusion,
            rawText = rawText
        )
    }
    
    override suspend fun deleteSummary(summary: SummaryEntity) {
        summaryDao.delete(summary)
    }
    
    override suspend fun deleteSummaryById(summaryId: Long) {
        summaryDao.deleteById(summaryId)
    }
    
    override suspend fun deleteSummariesByRecordId(recordId: Long) {
        summaryDao.deleteByRecordId(recordId)
    }
    
    override suspend fun getSummaryById(summaryId: Long): SummaryEntity? {
        return summaryDao.getById(summaryId)
    }
    
    override fun getSummaryByIdFlow(summaryId: Long): Flow<SummaryEntity?> {
        return summaryDao.getByIdFlow(summaryId)
    }
    
    override suspend fun getSummaryByRecordId(recordId: Long): SummaryEntity? {
        return summaryDao.getByRecordId(recordId)
    }
    
    override fun getSummaryByRecordIdFlow(recordId: Long): Flow<SummaryEntity?> {
        return summaryDao.getByRecordIdFlow(recordId)
    }
    
    override fun getAllSummaries(): Flow<List<SummaryEntity>> {
        return summaryDao.getAllSummaries()
    }
    
    override fun getRecentSummaries(limit: Int): Flow<List<SummaryEntity>> {
        return summaryDao.getRecentSummaries(limit)
    }
    
    override fun searchSummaries(keyword: String): Flow<List<SummaryEntity>> {
        return summaryDao.searchSummaries(keyword)
    }
    
    override suspend fun getSummaryCount(): Int {
        return summaryDao.getSummaryCount()
    }
    
    override suspend fun summaryExistsForRecord(recordId: Long): Boolean {
        return summaryDao.existsByRecordId(recordId)
    }
}
