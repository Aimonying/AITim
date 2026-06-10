package com.tingjizhushou.data.repository

import com.tingjizhushou.data.local.db.dao.TranscriptDao
import com.tingjizhushou.data.local.db.entity.TranscriptEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TranscriptRepository.
 * Provides concrete implementation of transcript operations using Room DAO.
 */
@Singleton
class TranscriptRepositoryImpl @Inject constructor(
    private val transcriptDao: TranscriptDao
) : TranscriptRepository {
    
    override suspend fun insertTranscript(transcript: TranscriptEntity): Long {
        return transcriptDao.insert(transcript)
    }
    
    override suspend fun insertTranscripts(transcripts: List<TranscriptEntity>): List<Long> {
        return transcriptDao.insertAll(transcripts)
    }
    
    override suspend fun updateTranscript(transcript: TranscriptEntity) {
        transcriptDao.update(transcript)
    }
    
    override suspend fun deleteTranscript(transcript: TranscriptEntity) {
        transcriptDao.delete(transcript)
    }
    
    override suspend fun deleteTranscriptById(transcriptId: Long) {
        transcriptDao.deleteById(transcriptId)
    }
    
    override suspend fun deleteTranscriptsByRecordId(recordId: Long) {
        transcriptDao.deleteByRecordId(recordId)
    }
    
    override suspend fun getTranscriptById(transcriptId: Long): TranscriptEntity? {
        return transcriptDao.getById(transcriptId)
    }
    
    override fun getTranscriptsByRecordId(recordId: Long): Flow<List<TranscriptEntity>> {
        return transcriptDao.getByRecordId(recordId)
    }
    
    override suspend fun getTranscriptsByRecordIdSync(recordId: Long): List<TranscriptEntity> {
        return transcriptDao.getByRecordIdSync(recordId)
    }
    
    override suspend fun getLatestTranscript(recordId: Long): TranscriptEntity? {
        return transcriptDao.getLatestByRecordId(recordId)
    }
    
    override fun getAllTranscripts(): Flow<List<TranscriptEntity>> {
        return transcriptDao.getAllTranscripts()
    }
    
    override fun getTranscriptsByEngine(engine: String): Flow<List<TranscriptEntity>> {
        return transcriptDao.getTranscriptsByEngine(engine)
    }
    
    override suspend fun getTranscriptCount(recordId: Long): Int {
        return transcriptDao.getCountByRecordId(recordId)
    }
}
