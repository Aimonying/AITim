package com.tingjizhushou.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tingjizhushou.data.RecordEntity
import com.tingjizhushou.data.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 记录ViewModel
 * 管理UI状态和业务逻辑
 */
class RecordViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = RecordRepository(application)
    
    // UI状态
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()
    
    // 所有记录
    val allRecords = repository.getAllRecords()
    
    // 搜索结果
    private val _searchResults = MutableStateFlow<List<RecordEntity>>(emptyList())
    val searchResults: StateFlow<List<RecordEntity>> = _searchResults.asStateFlow()
    
    /**
     * 插入记录
     */
    fun insertRecord(record: RecordEntity, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertRecord(record)
            launch(Dispatchers.Main) {
                onComplete(id)
            }
        }
    }
    
    /**
     * 更新记录
     */
    fun updateRecord(record: RecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecord(record)
        }
    }
    
    /**
     * 删除记录
     */
    fun deleteRecord(record: RecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecord(record)
        }
    }
    
    /**
     * 根据ID获取记录
     */
    fun getRecordById(id: Long, onComplete: (RecordEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = repository.getRecordById(id)
            launch(Dispatchers.Main) {
                onComplete(record)
            }
        }
    }
    
    /**
     * 搜索记录
     */
    fun searchRecords(keyword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.searchRecords(keyword).collect { records ->
                _searchResults.value = records
            }
        }
    }
    
    /**
     * 切换收藏状态
     */
    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(id, isFavorite)
        }
    }
    
    /**
     * 更新转写文字
     */
    fun updateTranscript(id: Long, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTranscript(id, text)
        }
    }
    
    /**
     * 更新会议纪要
     */
    fun updateMeetingMinutes(id: Long, minutes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMeetingMinutes(id, minutes)
        }
    }
    
    /**
     * 更新音频信息
     */
    fun updateAudioInfo(id: Long, path: String, size: Long, duration: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAudioInfo(id, path, size, duration)
        }
    }
    
    /**
     * 更新UI状态
     */
    fun updateUiState(state: RecordUiState) {
        _uiState.value = state
    }
}

/**
 * UI状态数据类
 */
data class RecordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val currentRecordId: Long? = null,
    val transcriptText: String = "",
    val meetingMinutes: String = "",
    val isRecording: Boolean = false,
    val recordingDuration: Int = 0,
    val audioFilePath: String? = null
)
