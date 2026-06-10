package com.tingjizhushou.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tingjizhushou.data.local.db.entity.RecordEntity
import com.tingjizhushou.data.repository.RecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 历史记录页面ViewModel
 * 管理记录列表、搜索、筛选等
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val recordRepository: RecordRepository
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    // 所有记录
    private val _records = MutableStateFlow<List<RecordEntity>>(emptyList())
    val records: StateFlow<List<RecordEntity>> = _records.asStateFlow()
    
    // 搜索关键词
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 是否只显示收藏
    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()
    
    // 当前筛选类型
    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()
    
    init {
        loadRecords()
    }
    
    /**
     * 加载所有记录
     */
    fun loadRecords() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            recordRepository.getAllRecords().collect { recordList ->
                _records.value = filterRecords(recordList)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * 搜索记录
     */
    fun searchRecords(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadRecords()
            } else {
                recordRepository.searchRecords(query).collect { results ->
                    _records.value = filterRecords(results)
                }
            }
        }
    }
    
    /**
     * 切换收藏筛选
     */
    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
        loadRecords()
    }
    
    /**
     * 按类型筛选
     */
    fun filterByType(type: String?) {
        _selectedType.value = type
        loadRecords()
    }
    
    /**
     * 删除记录
     */
    fun deleteRecord(record: RecordEntity) {
        viewModelScope.launch {
            try {
                recordRepository.deleteRecord(record)
                loadRecords()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "删除失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 切换收藏状态
     */
    fun toggleFavorite(record: RecordEntity) {
        viewModelScope.launch {
            try {
                recordRepository.toggleFavorite(record.id, !record.isFavorite)
                loadRecords()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "更新收藏状态失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 更新记录标题
     */
    fun updateRecordTitle(record: RecordEntity, newTitle: String) {
        viewModelScope.launch {
            try {
                val updated = record.copy(
                    title = newTitle,
                    updatedAt = System.currentTimeMillis()
                )
                recordRepository.updateRecord(updated)
                loadRecords()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "更新标题失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 根据条件筛选记录
     */
    private fun filterRecords(records: List<RecordEntity>): List<RecordEntity> {
        return records.filter { record ->
            val typeMatch = _selectedType.value?.let { it == record.type } ?: true
            val favoriteMatch = if (_showFavoritesOnly.value) record.isFavorite else true
            typeMatch && favoriteMatch
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
 * 历史记录UI状态
 */
data class HistoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
