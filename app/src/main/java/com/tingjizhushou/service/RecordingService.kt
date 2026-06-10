package com.tingjizhushou.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tingjizhushou.R
import com.tingjizhushou.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Foreground service for audio recording.
 * Handles recording audio from the microphone and saving to files.
 */
class RecordingService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "recording_channel"
        
        // Audio configuration
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private val binder = RecordingBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var outputFile: File? = null
    private var outputStream: FileOutputStream? = null
    
    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    enum class RecordingState {
        IDLE,
        RECORDING,
        PAUSED
    }

    inner class RecordingBinder : Binder() {
        fun getService(): RecordingService = this@RecordingService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        serviceScope.cancel()
    }

    /**
     * Start recording audio.
     */
    fun startRecording(): Boolean {
        if (_recordingState.value == RecordingState.RECORDING) {
            return false
        }

        // Calculate buffer size
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return false
            }

            // Create output file
            val recordingsDir = File(filesDir, "recordings")
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            outputFile = File(recordingsDir, "recording_$timestamp.pcm")
            outputStream = FileOutputStream(outputFile)

            // Start foreground service
            startForegroundService()
            
            // Start audio recording
            audioRecord?.startRecording()
            _recordingState.value = RecordingState.RECORDING
            _recordingDuration.value = 0L

            // Start recording coroutine
            recordingJob = serviceScope.launch {
                val buffer = ByteArray(bufferSize)
                val startTime = System.currentTimeMillis()
                
                while (isActive && _recordingState.value == RecordingState.RECORDING) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        outputStream?.write(buffer, 0, read)
                        _recordingDuration.value = System.currentTimeMillis() - startTime
                    }
                }
            }

            return true
        } catch (e: SecurityException) {
            return false
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Pause recording.
     */
    fun pauseRecording() {
        if (_recordingState.value == RecordingState.RECORDING) {
            _recordingState.value = RecordingState.PAUSED
            audioRecord?.stop()
        }
    }

    /**
     * Resume recording.
     */
    fun resumeRecording() {
        if (_recordingState.value == RecordingState.PAUSED) {
            audioRecord?.startRecording()
            _recordingState.value = RecordingState.RECORDING
            
            // Restart the recording job with updated start time
            recordingJob?.cancel()
            recordingJob = serviceScope.launch {
                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT
                )
                val buffer = ByteArray(bufferSize)
                val pausedDuration = _recordingDuration.value
                val resumeTime = System.currentTimeMillis() - pausedDuration
                
                while (isActive && _recordingState.value == RecordingState.RECORDING) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        outputStream?.write(buffer, 0, read)
                        _recordingDuration.value = System.currentTimeMillis() - resumeTime + pausedDuration
                    }
                }
            }
        }
    }

    /**
     * Stop recording and save file.
     */
    fun stopRecording(): RecordingResult? {
        recordingJob?.cancel()
        recordingJob = null
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            
            outputStream?.close()
            outputStream = null
            
            val file = outputFile
            outputFile = null
            
            _recordingState.value = RecordingState.IDLE
            _recordingDuration.value = 0L
            
            stopForeground(STOP_FOREGROUND_REMOVE)
            
            return file?.let { 
                RecordingResult(it.absolutePath, it.length(), SAMPLE_RATE) 
            }
        } catch (e: Exception) {
            _recordingState.value = RecordingState.IDLE
            return null
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.recording_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Recording notification channel"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.recording_notification_title))
            .setContentText(getString(R.string.recording_notification_text))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    data class RecordingResult(
        val filePath: String,
        val fileSize: Long,
        val sampleRate: Int
    )
}
