package com.tingjizhushou.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * 记录数据仓库
 * 统一数据访问入口，封装数据源逻辑
 */
class RecordRepository(private val context: Context) {
    
    private val recordDao = (context.applicationContext as TingJiApplication).database.recordDao()
    
    // 获取所有记录
    fun getAllRecords(): Flow<List<RecordEntity>> {
        return recordDao.getAll()
    }
    
    // 根据ID获取记录
    suspend fun getRecordById(id: Long): RecordEntity? {
        return recordDao.getById(id)
    }
    
    // 插入记录
    suspend fun insertRecord(record: RecordEntity): Long {
        return recordDao.insert(record)
    }
    
    // 更新记录
    suspend fun updateRecord(record: RecordEntity) {
        recordDao.update(record)
    }
    
    // 删除记录
    suspend fun deleteRecord(record: RecordEntity) {
        // 同时删除关联的音频文件
        record.audioPath?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        recordDao.delete(record)
    }
    
    // 搜索记录
    fun searchRecords(keyword: String): Flow<List<RecordEntity>> {
        return recordDao.search(keyword)
    }
    
    // 获取收藏记录
    fun getFavoriteRecords(): Flow<List<RecordEntity>> {
        return recordDao.getFavorites()
    }
    
    // 切换收藏状态
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        recordDao.setFavorite(id, isFavorite)
    }
    
    // 更新转写文字
    suspend fun updateTranscript(id: Long, text: String) {
        recordDao.updateTranscript(id, text, java.util.Date())
    }
    
    // 更新会议纪要
    suspend fun updateMeetingMinutes(id: Long, minutes: String) {
        recordDao.updateMeetingMinutes(id, minutes, java.util.Date())
    }
    
    // 更新音频信息
    suspend fun updateAudioInfo(id: Long, path: String, size: Long, duration: Int) {
        recordDao.updateAudioInfo(id, path, size, duration)
    }
    
    // 根据类型获取记录
    fun getRecordsByType(type: String): Flow<List<RecordEntity>> {
        return recordDao.getByType(type)
    }
    
    // 获取记录总数
    suspend fun getRecordCount(): Int {
        return recordDao.getCount()
    }
}
