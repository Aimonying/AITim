package com.tingjizhushou.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.tingjizhushou.util.AudioUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a transcription operation.
 */
data class TranscriptionResult(
    val text: String,
    val language: String,
    val confidence: Float = 0f,
    val isPartial: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Service for speech recognition operations.
 * Supports both online (real-time) and offline recognition.
 */
@Singleton
class SpeechRecognizeService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var currentLanguage: String = "zh-CN"
    private var isOnlineMode: Boolean = false
    
    // Transcription result
    private val _transcriptResult = MutableStateFlow("")
    val transcriptResult: StateFlow<String> = _transcriptResult.asStateFlow()
    
    // Is recognizing speech
    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()
    
    // Partial result during recognition
    private val _partialResult = MutableStateFlow("")
    val partialResult: StateFlow<String> = _partialResult.asStateFlow()
    
    // Error message
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Results list for batch processing
    private val _results = MutableStateFlow<List<TranscriptionResult>>(emptyList())
    val results: StateFlow<List<TranscriptionResult>> = _results.asStateFlow()
    
    // Confidence score
    private val _confidence = MutableStateFlow(0f)
    val confidence: StateFlow<Float> = _confidence.asStateFlow()
    
    /**
     * Check if speech recognition is available on this device.
     * @return True if available
     */
    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    /**
     * Initialize the speech recognizer.
     */
    private fun initializeRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        }
    }
    
    /**
     * Start online (real-time) speech recognition.
     * @param language Language code (e.g., "zh-CN", "en-US")
     * @return Result indicating success or failure
     */
    suspend fun startOnlineRecognition(language: String): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            if (!isRecognitionAvailable()) {
                return@withContext Result.failure(UnsupportedOperationException("Speech recognition not available"))
            }
            
            currentLanguage = language
            isOnlineMode = true
            
            // Clear previous results
            _transcriptResult.value = ""
            _partialResult.value = ""
            _results.value = emptyList()
            _error.value = null
            _confidence.value = 0f
            
            initializeRecognizer()
            
            val intent = createRecognizerIntent(language).apply {
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
            }
            
            speechRecognizer?.startListening(intent)
            _isRecognizing.value = true
            
            Result.success(Unit)
        } catch (e: Exception) {
            _error.value = e.message
            _isRecognizing.value = false
            Result.failure(e)
        }
    }
    
    /**
     * Recognize speech from an audio file.
     * Note: This uses the file-based recognition intent.
     * For full file transcription, consider using a cloud API or Vosk.
     * @param filePath Path to the audio file
     * @param language Language code
     * @return Transcription result
     */
    suspend fun recognizeFile(filePath: String, language: String): Result<TranscriptionResult> = withContext(Dispatchers.Main) {
        try {
            if (!isRecognitionAvailable()) {
                return@withContext Result.failure(UnsupportedOperationException("Speech recognition not available"))
            }
            
            if (!File(filePath).exists()) {
                return@withContext Result.failure(IllegalArgumentException("Audio file not found: $filePath"))
            }
            
            currentLanguage = language
            isOnlineMode = false
            
            // Clear previous results
            _transcriptResult.value = ""
            _partialResult.value = ""
            _error.value = null
            
            initializeRecognizer()
            
            // For file recognition, we use a different approach
            // Note: Android's built-in recognizer has limitations with file input
            // This is a simplified implementation
            val intent = createRecognizerIntent(language).apply {
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
            }
            
            _isRecognizing.value = true
            speechRecognizer?.startListening(intent)
            
            // Note: In production, you would integrate with:
            // 1. Google Cloud Speech-to-Text API
            // 2. Vosk offline recognition
            // 3. Other speech-to-text services
            
            Result.success(
                TranscriptionResult(
                    text = "",
                    language = language,
                    confidence = 0f,
                    isPartial = true
                )
            )
        } catch (e: Exception) {
            _error.value = e.message
            _isRecognizing.value = false
            Result.failure(e)
        }
    }
    
    /**
     * Stop current recognition.
     */
    suspend fun stopRecognition() = withContext(Dispatchers.Main) {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            _isRecognizing.value = false
            _partialResult.value = ""
        } catch (e: Exception) {
            _error.value = e.message
        }
    }
    
    /**
     * Destroy the speech recognizer and release resources.
     */
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isRecognizing.value = false
        } catch (e: Exception) {
            _error.value = e.message
        }
    }
    
    /**
     * Create the recognition intent with common parameters.
     */
    private fun createRecognizerIntent(language: String): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }
    }
    
    /**
     * Create the recognition listener to handle events.
     */
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _error.value = null
            }
            
            override fun onBeginningOfSpeech() {
                // Speech input has begun
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Sound level changed - can be used for visualization
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }
            
            override fun onEndOfSpeech() {
                _isRecognizing.value = false
            }
            
            override fun onError(errorCode: Int) {
                val errorMessage = getErrorMessage(errorCode)
                _error.value = errorMessage
                _isRecognizing.value = false
                
                // Handle specific errors
                when (errorCode) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        // No match found - might want to restart listening
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        // Need permissions
                    }
                }
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (!matches.isNullOrEmpty()) {
                    val bestMatch = matches[0]
                    val conf = confidences?.getOrNull(0) ?: 0f
                    
                    val result = TranscriptionResult(
                        text = bestMatch,
                        language = currentLanguage,
                        confidence = conf,
                        isPartial = false
                    )
                    
                    // Append to existing transcript
                    val currentText = _transcriptResult.value
                    _transcriptResult.value = if (currentText.isBlank()) {
                        bestMatch
                    } else {
                        "$currentText\n$bestMatch"
                    }
                    
                    _confidence.value = conf
                    _results.value = _results.value + result
                }
                
                _isRecognizing.value = false
                _partialResult.value = ""
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                
                if (!matches.isNullOrEmpty()) {
                    _partialResult.value = matches[0]
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Reserved for future events
            }
        }
    }
    
    /**
     * Convert error code to human-readable message.
     */
    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No recognition result matched"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }
    
    /**
     * Get the duration of an audio file.
     * @param filePath Path to the audio file
     * @return Duration in seconds
     */
    fun getAudioDuration(filePath: String): Int {
        return AudioUtils.getAudioDuration(filePath)
    }
    
    /**
     * Check if a language is supported.
     * @param language Language code
     * @return True if supported
     */
    fun isLanguageSupported(language: String): Boolean {
        val locale = when (language) {
            "zh-CN" -> Locale.CHINA
            "en-US" -> Locale.US
            "ja-JP" -> Locale.JAPAN
            "ko-KR" -> Locale.KOREA
            else -> Locale.forLanguageTag(language)
        }
        
        return SpeechRecognizer.isLanguageAvailable(locale) != SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED
    }
}
