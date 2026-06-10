package com.tingjizhushou.util

import android.media.AudioFormat
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.util.Locale

/**
 * Audio-related constants for recording configuration.
 */
object AudioConstants {
    /** Sample rate for audio recording (44.1 kHz) */
    const val SAMPLE_RATE = 44100
    
    /** Channel configuration for mono recording */
    const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    
    /** Audio encoding format (16-bit PCM) */
    const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    
    /** Default recording bit rate (128 kbps) */
    const val DEFAULT_BIT_RATE = 128000
    
    /** Maximum amplitude value for normalization */
    const val MAX_AMPLITUDE = 32767
    
    /** Minimum amplitude threshold for silence detection */
    const val SILENCE_THRESHOLD = 500
}

/**
 * Audio utility functions for duration formatting and amplitude calculations.
 */
object AudioUtils {
    
    /**
     * Format duration in seconds to human-readable string.
     * @param seconds Duration in seconds
     * @return Formatted string (e.g., "01:23:45")
     */
    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        }
    }
    
    /**
     * Format duration in milliseconds to human-readable string.
     * @param millis Duration in milliseconds
     * @return Formatted string (e.g., "01:23:45")
     */
    fun formatDurationMillis(millis: Long): String {
        return formatDuration((millis / 1000).toInt())
    }
    
    /**
     * Convert amplitude to normalized level (0.0 - 1.0).
     * @param amplitude Raw amplitude value
     * @return Normalized level
     */
    fun getAmplitudeLevel(amplitude: Int): Float {
        // Amplitude range is typically 0-32767 for 16-bit audio
        val normalized = amplitude.toFloat() / AudioConstants.MAX_AMPLITUDE
        return normalized.coerceIn(0f, 1f)
    }
    
    /**
     * Convert decibels to linear scale (0.0 - 1.0).
     * @param decibels Amplitude in dB
     * @return Linear amplitude level
     */
    fun dbToLinear(decibels: Int): Float {
        if (decibels <= 0) return 0f
        val linear = Math.pow(10.0, decibels.toDouble() / 20.0)
        return (linear / AudioConstants.MAX_AMPLITUDE).toFloat().coerceIn(0f, 1f)
    }
    
    /**
     * Get audio duration from a file.
     * @param filePath Path to the audio file
     * @return Duration in seconds, -1 if unable to determine
     */
    fun getAudioDuration(filePath: String): Int {
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            
            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }
            
            if (audioTrackIndex < 0) {
                extractor.release()
                return -1
            }
            
            val format = extractor.getTrackFormat(audioTrackIndex)
            val durationUs = format.getLong(MediaFormat.KEY_DURATION)
            
            extractor.release()
            
            // Convert microseconds to seconds
            (durationUs / 1_000_000).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
    
    /**
     * Get audio duration in milliseconds from a file.
     * @param filePath Path to the audio file
     * @return Duration in milliseconds, -1 if unable to determine
     */
    fun getAudioDurationMillis(filePath: String): Long {
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            
            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }
            
            if (audioTrackIndex < 0) {
                extractor.release()
                return -1
            }
            
            val format = extractor.getTrackFormat(audioTrackIndex)
            val durationUs = format.getLong(MediaFormat.KEY_DURATION)
            
            extractor.release()
            
            durationUs / 1000
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
    
    /**
     * Check if the audio file is silence (amplitude below threshold).
     * @param amplitude Current amplitude
     * @return True if considered silence
     */
    fun isSilence(amplitude: Int): Boolean {
        return amplitude < AudioConstants.SILENCE_THRESHOLD
    }
    
    /**
     * Get sample rate from audio file.
     * @param filePath Path to the audio file
     * @return Sample rate in Hz, -1 if unable to determine
     */
    fun getSampleRate(filePath: String): Int {
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    extractor.release()
                    return sampleRate
                }
            }
            
            extractor.release()
            -1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
    
    /**
     * Calculate estimated file size for given duration and bitrate.
     * @param durationSeconds Duration in seconds
     * @param bitRate Bitrate in bits per second
     * @return Estimated file size in bytes
     */
    fun estimateFileSize(durationSeconds: Int, bitRate: Int = AudioConstants.DEFAULT_BIT_RATE): Long {
        return (durationSeconds.toLong() * bitRate) / 8
    }
    
    /**
     * Get audio bitrate from file.
     * @param filePath Path to the audio file
     * @return Bitrate in bits per second, -1 if unable to determine
     */
    fun getBitRate(filePath: String): Int {
        return try {
            val file = File(filePath)
            val durationMs = getAudioDurationMillis(filePath)
            if (durationMs <= 0) return -1
            
            val sizeBytes = file.length()
            val durationSec = durationMs / 1000.0
            
            ((sizeBytes * 8) / durationSec).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
}
