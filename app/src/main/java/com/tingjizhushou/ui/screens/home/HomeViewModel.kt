package com.tingjizhushou.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tingjizhushou.data.local.db.entity.RecordEntity
import com.tingjizhushou.data.repository.RecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen.
 * Provides recent records and statistics.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recordRepository: RecordRepository
) : ViewModel() {
    
    /**
     * Recent records (limited to 5).
     */
    val recentRecords: StateFlow<List<RecordEntity>> = recordRepository
        .getRecentRecords(5)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * All records for statistics.
     */
    val allRecords: StateFlow<List<RecordEntity>> = recordRepository
        .getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Total record count.
     */
    private val _totalRecordCount = MutableStateFlow(0)
    val totalRecordCount: StateFlow<Int> = _totalRecordCount.asStateFlow()
    
    /**
     * Favorite records count.
     */
    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount: StateFlow<Int> = _favoriteCount.asStateFlow()
    
    /**
     * Total recording time in seconds.
     */
    private val _totalRecordingTime = MutableStateFlow(0L)
    val totalRecordingTime: StateFlow<Long> = _totalRecordingTime.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    /**
     * Load statistics from records.
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            recordRepository.getAllRecords().collect { records ->
                _totalRecordCount.value = records.size
                _favoriteCount.value = records.count { it.isFavorite }
                _totalRecordingTime.value = records.sumOf { it.duration.toLong() }
            }
        }
    }
    
    /**
     * Refresh statistics.
     */
    fun refreshStatistics() {
        loadStatistics()
    }
    
    /**
     * Delete a record.
     */
    fun deleteRecord(record: RecordEntity) {
        viewModelScope.launch {
            recordRepository.deleteRecord(record)
        }
    }
    
    /**
     * Toggle favorite status.
     */
    fun toggleFavorite(recordId: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            recordRepository.toggleFavorite(recordId, !currentFavorite)
        }
    }
}
