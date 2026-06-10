package com.tingjizhushou.ui.screens.recording

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tingjizhushou.data.local.db.entity.RecordEntity
import com.tingjizhushou.data.repository.RecordRepository
import com.tingjizhushou.service.AudioRecordService
import com.tingjizhushou.service.SpeechRecognizeService
import com.tingjizhushou.service.SummaryGeneratorService
import com.tingjizhushou.ui.state.RecordingState
import com.tingjizhushou.ui.state.RecordingStatus
import com.tingjizhushou.ui.state.TranscribeState
import com.tingjizhushou.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 录音页面ViewModel
 * 管理录音、转写、生成纪要的完整流程
 */
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val application: Application,
    private val audioRecordService: AudioRecordService,
    private val speechRecognizeService: SpeechRecognizeService,
    private val summaryGeneratorService: SummaryGeneratorService,
    private val recordRepository: RecordRepository
) : AndroidViewModel(application) {
    
    // 录音状态
    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    // 转写状态
    private val _transcribeState = MutableStateFlow(TranscribeState())
    val transcribeState: StateFlow<TranscribeState> = _transcribeState.asStateFlow()
    
    // 当前记录ID
    private var currentRecordId: Long? = null
    
    // 计时器Job
    private var timerJob: Job? = null
    
    // 录音文件路径
    private var audioFilePath: String? = null
    
    /**
     * 开始录音
     */
    fun startRecording(language: String = "zh-CN") {
        viewModelScope.launch {
            try {
                // 生成音频文件路径
                val audioDir = FileUtils.getAudioDirectory(application)
                val fileName = FileUtils.generateAudioFileName()
                audioFilePath = "${audioDir.absolutePath}/$fileName"
                
                // 更新状态
                _recordingState.value = _recordingState.value.copy(
                    isRecording = true,
                    isPaused = false,
                    duration = 0L,
                    fileSize = 0L,
                    language = language,
                    error = null
                )
                
                // 启动录音服务
                audioRecordService.startRecording(audioFilePath!!)
                
                // 开始计时
                startTimer()
                
            } catch (e: Exception) {
                _recordingState.value = _recordingState.value.copy(
                    isRecording = false,
                    error = "启动录音失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 暂停录音
     */
    fun pauseRecording() {
        viewModelScope.launch {
            try {
                audioRecordService.pauseRecording()
                timerJob?.cancel()
                
                _recordingState.value = _recordingState.value.copy(
                    isPaused = true
                )
            } catch (e: Exception) {
                _recordingState.value = _recordingState.value.copy(
                    error = "暂停录音失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 继续录音
     */
    fun resumeRecording() {
        viewModelScope.launch {
            try {
                audioRecordService.resumeRecording()
                startTimer()
                
                _recordingState.value = _recordingState.value.copy(
                    isPaused = false
                )
            } catch (e: Exception) {
                _recordingState.value = _recordingState.value.copy(
                    error = "继续录音失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 停止录音
     */
    fun stopRecording() {
        viewModelScope.launch {
            try {
                timerJob?.cancel()
                
                val result = audioRecordService.stopRecording()
                result.onSuccess { path ->
                    audioFilePath = path
                    
                    _recordingState.value = _recordingState.value.copy(
                        isRecording = false,
                        isPaused = false,
                        audioFilePath = path
                    )
                    
                    // 开始转写
                    startTranscription()
                }
                
                result.onFailure { e ->
                    _recordingState.value = _recordingState.value.copy(
                        isRecording = false,
                        error = "停止录音失败: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _recordingState.value = _recordingState.value.copy(
                    isRecording = false,
                    error = "停止录音失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 取消录音
     */
    fun cancelRecording() {
        viewModelScope.launch {
            timerJob?.cancel()
            audioRecordService.cancelRecording()
            
            _recordingState.value = RecordingState()
            _transcribeState.value = TranscribeState()
            audioFilePath = null
            currentRecordId = null
        }
    }
    
    /**
     * 保存记录
     */
    fun saveRecord(
        title: String,
        saveAudio: Boolean,
        saveTranscript: Boolean,
        saveSummary: Boolean
    ) {
        viewModelScope.launch {
            val recordingState = _recordingState.value
            val transcribeState = _transcribeState.value
            
            // 创建记录
            val record = RecordEntity(
                title = title,
                type = "REALTIME",
                createdAt = System.currentTimeMillis(),
                audioFilePath = if (saveAudio) audioFilePath else null,
                audioSize = recordingState.fileSize,
                duration = recordingState.duration.toInt(),
                transcriptText = if (saveTranscript) transcribeState.finalResult else null,
                meetingMinutes = if (saveSummary) generateSummaryFromText(transcribeState.finalResult) else null,
                language = recordingState.language
            )
            
            currentRecordId = recordRepository.insertRecord(record)
        }
    }
    
    /**
     * 设置语言
     */
    fun setLanguage(language: String) {
        _recordingState.value = _recordingState.value.copy(language = language)
        _transcribeState.value = _transcribeState.value.copy(language = language)
    }
    
    /**
     * 开始计时
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                
                val currentState = audioRecordService.recordingDuration.value
                val fileSize = audioRecordService.fileSize.value
                val amplitude = audioRecordService.amplitude.value
                
                _recordingState.value = _recordingState.value.copy(
                    duration = currentState,
                    fileSize = fileSize,
                    amplitude = amplitude
                )
                
                // 检查500MB限制
                if (fileSize >= 500 * 1024 * 1024) {
                    // 自动暂停并提示
                    pauseRecording()
                    _recordingState.value = _recordingState.value.copy(
                        error = "已达到500MB文件大小限制，是否开始新录音？"
                    )
                }
            }
        }
    }
    
    /**
     * 开始转写
     */
    private fun startTranscription() {
        viewModelScope.launch {
            _transcribeState.value = _transcribeState.value.copy(
                isTranscribing = true,
                progress = 0f
            )
            
            try {
                // 使用语音识别服务
                speechRecognizeService.startOnlineRecognition(
                    _recordingState.value.language
                ).onSuccess {
                    // 收集识别结果
                    speechRecognizeService.transcriptResult.collect { result ->
                        _transcribeState.value = _transcribeState.value.copy(
                            finalResult = result,
                            isTranscribing = false
                        )
                    }
                }.onFailure { e ->
                    _transcribeState.value = _transcribeState.value.copy(
                        isTranscribing = false,
                        error = "转写失败: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _transcribeState.value = _transcribeState.value.copy(
                    isTranscribing = false,
                    error = "转写失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 从文本生成纪要
     */
    private suspend fun generateSummaryFromText(text: String): String? {
        return try {
            val result = summaryGeneratorService.generateSummary(
                text,
                _recordingState.value.language
            )
            result.getOrNull()?.meetingMinutes
        } catch (e: Exception) {
            null
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
