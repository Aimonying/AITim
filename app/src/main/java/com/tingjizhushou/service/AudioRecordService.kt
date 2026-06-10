package com.tingjizhushou.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.tingjizhushou.util.AudioConstants
import com.tingjizhushou.util.FileConstants
import com.tingjizhushou.util.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recording state representing the current status of audio recording.
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED
}

/**
 * Service for handling audio recording operations.
 * Uses MediaRecorder to capture audio with support for pause/resume.
 * Monitors file size to enforce 500MB limit.
 */
@Singleton
class AudioRecordService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null
    private var recordingStartTime: Long = 0
    private var pausedDuration: Long = 0
    private var pauseStartTime: Long = 0
    
    // Recording state
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    // Is recording (convenience property)
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    // Recording duration in milliseconds
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()
    
    // Current file size in bytes
    private val _fileSize = MutableStateFlow(0L)
    val fileSize: StateFlow<Long> = _fileSize.asStateFlow()
    
    // Current amplitude for visualization
    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()
    
    // Error message if any
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Start audio recording to the specified file path.
     * @param outputPath Full path for the output audio file
     * @return Result indicating success or failure
     */
    suspend fun startRecording(outputPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validate output path
            if (outputPath.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Output path cannot be empty"))
            }
            
            // Ensure parent directory exists
            val file = File(outputPath)
            if (file.parentFile != null && !file.parentFile!!.exists()) {
                file.parentFile!!.mkdirs()
            }
            
            // Check if already recording
            if (_isRecording.value) {
                return@withContext Result.failure(IllegalStateException("Already recording"))
            }
            
            // Initialize MediaRecorder
            mediaRecorder = createMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(AudioConstants.SAMPLE_RATE)
                setAudioEncodingBitRate(AudioConstants.DEFAULT_BIT_RATE)
                setOutputFile(outputPath)
                
                prepare()
                start()
            }
            
            currentFilePath = outputPath
            recordingStartTime = System.currentTimeMillis()
            pausedDuration = 0
            _recordingState.value = RecordingState.RECORDING
            _isRecording.value = true
            _error.value = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            _error.value = e.message
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Pause the current recording.
     * @return Result indicating success or failure
     */
    suspend fun pauseRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (_recordingState.value != RecordingState.RECORDING) {
                return@withContext Result.failure(IllegalStateException("Not currently recording"))
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                pauseStartTime = System.currentTimeMillis()
                _recordingState.value = RecordingState.PAUSED
                Result.success(Unit)
            } else {
                Result.failure(UnsupportedOperationException("Pause not supported on this Android version"))
            }
        } catch (e: Exception) {
            _error.value = e.message
            Result.failure(e)
        }
    }
    
    /**
     * Resume a paused recording.
     * @return Result indicating success or failure
     */
    suspend fun resumeRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (_recordingState.value != RecordingState.PAUSED) {
                return@withContext Result.failure(IllegalStateException("Recording is not paused"))
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                pausedDuration += System.currentTimeMillis() - pauseStartTime
                _recordingState.value = RecordingState.RECORDING
                Result.success(Unit)
            } else {
                Result.failure(UnsupportedOperationException("Resume not supported on this Android version"))
            }
        } catch (e: Exception) {
            _error.value = e.message
            Result.failure(e)
        }
    }
    
    /**
     * Stop recording and return the file path.
     * @return Result with the file path or failure
     */
    suspend fun stopRecording(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (_recordingState.value == RecordingState.IDLE) {
                return@withContext Result.failure(IllegalStateException("Not currently recording"))
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            val filePath = currentFilePath ?: ""
            _recordingState.value = RecordingState.IDLE
            _isRecording.value = false
            _recordingDuration.value = 0L
            _fileSize.value = 0L
            _amplitude.value = 0
            
            if (filePath.isBlank()) {
                Result.failure(IllegalStateException("No file path recorded"))
            } else {
                Result.success(filePath)
            }
        } catch (e: Exception) {
            _error.value = e.message
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Cancel recording and delete the file.
     * @return Result indicating success or failure
     */
    suspend fun cancelRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val filePath = currentFilePath
            
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (ignored: Exception) {
                    // Ignore stop errors when cancelling
                }
                release()
            }
            mediaRecorder = null
            
            // Delete the file
            if (filePath != null) {
                FileUtils.deleteFile(filePath)
            }
            
            _recordingState.value = RecordingState.IDLE
            _isRecording.value = false
            _recordingDuration.value = 0L
            _fileSize.value = 0L
            _amplitude.value = 0
            currentFilePath = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            _error.value = e.message
            cleanup()
            Result.failure(e)
        }
    }
    
    /**
     * Update recording metrics (duration, file size, amplitude).
     * Should be called periodically during recording.
     */
    fun updateMetrics() {
        if (_recordingState.value != RecordingState.RECORDING) return
        
        // Calculate actual recording duration
        val currentTime = System.currentTimeMillis()
        val totalElapsed = currentTime - recordingStartTime
        val actualDuration = totalElapsed - pausedDuration - (currentTime - pauseStartTime)
        
        if (_recordingState.value == RecordingState.RECORDING) {
            _recordingDuration.value = totalElapsed - pausedDuration
        }
        
        // Update file size
        currentFilePath?.let { path ->
            _fileSize.value = FileUtils.getFileSize(path)
        }
        
        // Update amplitude
        try {
            _amplitude.value = mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            _amplitude.value = 0
        }
    }
    
    /**
     * Check if the current file size exceeds the 500MB limit.
     * @return True if limit exceeded
     */
    fun checkFileSizeLimit(): Boolean {
        val currentSize = _fileSize.value
        return currentSize >= FileConstants.MAX_FILE_SIZE_BYTES
    }
    
    /**
     * Check if the current file size exceeds the 500MB limit.
     * @param filePath Path to check
     * @return True if limit exceeded
     */
    fun checkFileSizeLimit(filePath: String): Boolean {
        return FileUtils.exceedsSizeLimit(filePath)
    }
    
    /**
     * Get remaining recording time based on current bitrate and file limit.
     * @return Estimated remaining seconds
     */
    fun getRemainingRecordingTime(): Long {
        val currentSize = _fileSize.value
        val remainingBytes = FileConstants.MAX_FILE_SIZE_BYTES - currentSize
        val bitRateBytesPerSecond = AudioConstants.DEFAULT_BIT_RATE / 8
        return remainingBytes / bitRateBytesPerSecond
    }
    
    /**
     * Get current recording duration in seconds.
     * @return Duration in seconds
     */
    fun getCurrentDurationSeconds(): Int {
        return (_recordingDuration.value / 1000).toInt()
    }
    
    /**
     * Create MediaRecorder instance with appropriate API level.
     */
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }
    
    /**
     * Cleanup resources.
     */
    private fun cleanup() {
        try {
            mediaRecorder?.release()
        } catch (ignored: Exception) {
        }
        mediaRecorder = null
        _recordingState.value = RecordingState.IDLE
        _isRecording.value = false
        _recordingDuration.value = 0L
        _fileSize.value = 0L
        _amplitude.value = 0
        currentFilePath = null
    }
    
    /**
     * Release resources when service is destroyed.
     */
    fun release() {
        cleanup()
    }
}
