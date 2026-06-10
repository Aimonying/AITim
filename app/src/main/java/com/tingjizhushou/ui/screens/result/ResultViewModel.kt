package com.tingjizhushou.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tingjizhushou.data.local.db.entity.RecordEntity
import com.tingjizhushou.data.repository.RecordRepository
import com.tingjizhushou.service.SummaryGeneratorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 结果页面ViewModel
 * 管理转写结果、纪要生成、保存等功能
 */
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val summaryGeneratorService: SummaryGeneratorService
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()
    
    // 当前记录
    private val _currentRecord = MutableStateFlow<RecordEntity?>(null)
    val currentRecord: StateFlow<RecordEntity?> = _currentRecord.asStateFlow()
    
    // 编辑后的转写文本
    private val _editedTranscript = MutableStateFlow("")
    val editedTranscript: StateFlow<String> = _editedTranscript.asStateFlow()
    
    // 保存选项
    private val _saveOptions = MutableStateFlow(SaveOptions())
    val saveOptions: StateFlow<SaveOptions> = _saveOptions.asStateFlow()
    
    /**
     * 加载记录
     */
    fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val record = recordRepository.getRecordById(recordId)
                record?.let {
                    _currentRecord.value = it
                    _editedTranscript.value = it.transcriptText ?: ""
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载记录失败: ${e.message}"
                )
            }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    /**
     * 更新转写文本
     */
    fun updateTranscript(text: String) {
        _editedTranscript.value = text
    }
    
    /**
     * 重新生成纪要
     */
    fun regenerateSummary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingSummary = true)
            
            try {
                val result = summaryGeneratorService.generateSummary(
                    _editedTranscript.value,
                    _currentRecord.value?.language ?: "zh-CN"
                )
                
                result.onSuccess { summary ->
                    _currentRecord.value = _currentRecord.value?.copy(
                        meetingMinutes = summary.meetingMinutes
                    )
                }
                
                result.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "生成纪要失败: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "生成纪要失败: ${e.message}"
                )
            }
            
            _uiState.value = _uiState.value.copy(isGeneratingSummary = false)
        }
    }
    
    /**
     * 更新保存选项
     */
    fun updateSaveOptions(options: SaveOptions) {
        _saveOptions.value = options
    }
    
    /**
     * 保存记录
     */
    fun saveRecord() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            try {
                val record = _currentRecord.value ?: return@launch
                val options = _saveOptions.value
                
                val updated = record.copy(
                    transcriptText = if (options.saveTranscript) _editedTranscript.value else record.transcriptText,
                    meetingMinutes = if (options.saveSummary) record.meetingMinutes else record.meetingMinutes,
                    updatedAt = System.currentTimeMillis()
                )
                
                // 如果不保存录音，删除音频文件
                if (!options.saveAudio && record.audioFilePath != null) {
                    // 文件会在Repository中自动删除
                }
                
                recordRepository.updateRecord(updated)
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "保存失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 分享内容
     */
    fun shareContent(): String {
        val record = _currentRecord.value ?: return ""
        val sb = StringBuilder()
        
        sb.appendLine("【${record.title}】")
        sb.appendLine()
        
        if (_saveOptions.value.saveTranscript && _editedTranscript.value.isNotBlank()) {
            sb.appendLine("【转写内容】")
            sb.appendLine(_editedTranscript.value)
            sb.appendLine()
        }
        
        if (_saveOptions.value.saveSummary && !record.meetingMinutes.isNullOrBlank()) {
            sb.appendLine("【会议纪要】")
            sb.appendLine(record.meetingMinutes)
        }
        
        return sb.toString()
    }
    
    /**
     * 删除记录
     */
    fun deleteRecord() {
        viewModelScope.launch {
            try {
                val record = _currentRecord.value ?: return@launch
                recordRepository.deleteRecord(record)
                _uiState.value = _uiState.value.copy(deleteSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "删除失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 结果页面UI状态
 */
data class ResultUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isGeneratingSummary: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

/**
 * 保存选项
 */
data class SaveOptions(
    val saveAudio: Boolean = true,
    val saveTranscript: Boolean = true,
    val saveSummary: Boolean = true
)
