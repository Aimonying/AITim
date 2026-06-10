package com.tingjizhushou.data.repository

import com.tingjizhushou.data.local.db.dao.RecordDao
import com.tingjizhushou.data.local.db.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RecordRepository.
 * Provides concrete implementation of record operations using Room DAO.
 */
@Singleton
class RecordRepositoryImpl @Inject constructor(
    private val recordDao: RecordDao
) : RecordRepository {
    
    override suspend fun insertRecord(record: RecordEntity): Long {
        return recordDao.insert(record)
    }
    
    override suspend fun updateRecord(record: RecordEntity) {
        recordDao.update(record)
    }
    
    override suspend fun deleteRecord(record: RecordEntity) {
        recordDao.delete(record)
    }
    
    override suspend fun deleteRecordById(recordId: Long) {
        recordDao.deleteById(recordId)
    }
    
    override suspend fun getRecordById(recordId: Long): RecordEntity? {
        return recordDao.getById(recordId)
    }
    
    override fun getRecordByIdFlow(recordId: Long): Flow<RecordEntity?> {
        return recordDao.getByIdFlow(recordId)
    }
    
    override fun getAllRecords(): Flow<List<RecordEntity>> {
        return recordDao.getAllRecords()
    }
    
    override fun getRecordsByType(type: String): Flow<List<RecordEntity>> {
        return recordDao.getRecordsByType(type)
    }
    
    override fun getRecordsByLanguage(language: String): Flow<List<RecordEntity>> {
        return recordDao.getRecordsByLanguage(language)
    }
    
    override fun searchRecords(keyword: String): Flow<List<RecordEntity>> {
        return recordDao.searchRecords(keyword)
    }
    
    override fun getFavoriteRecords(): Flow<List<RecordEntity>> {
        return recordDao.getFavoriteRecords()
    }
    
    override suspend fun toggleFavorite(recordId: Long, isFavorite: Boolean) {
        recordDao.setFavorite(recordId, isFavorite)
    }
    
    override suspend fun updateTranscript(recordId: Long, transcriptText: String) {
        recordDao.updateTranscript(recordId, transcriptText)
    }
    
    override suspend fun updateMeetingMinutes(recordId: Long, meetingMinutes: String) {
        recordDao.updateMeetingMinutes(recordId, meetingMinutes)
    }
    
    override fun getRecentRecords(limit: Int): Flow<List<RecordEntity>> {
        return recordDao.getRecentRecords(limit)
    }
    
    override suspend fun getRecordCount(): Int {
        return recordDao.getRecordCount()
    }
}
