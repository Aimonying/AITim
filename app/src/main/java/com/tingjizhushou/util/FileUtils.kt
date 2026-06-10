package com.tingjizhushou.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat

/**
 * File-related constants and utility functions.
 */
object FileConstants {
    /** Maximum allowed audio file size: 500MB */
    const val MAX_FILE_SIZE_BYTES = 500L * 1024 * 1024
    
    /** Directory name for storing audio files */
    const val AUDIO_DIRECTORY = "audio"
    
    /** File extension for transcripts */
    const val TRANSCRIPT_EXTENSION = ".txt"
    
    /** File extension for summaries */
    const val SUMMARY_EXTENSION = ".md"
    
    /** Transcript directory name */
    const val TRANSCRIPT_DIRECTORY = "transcripts"
    
    /** Summary directory name */
    const val SUMMARY_DIRECTORY = "summaries"
}

/**
 * File utility functions for managing audio, transcript, and summary files.
 */
object FileUtils {
    
    /**
     * Get or create the audio directory.
     * @param context Android context
     * @return The audio directory file
     */
    fun getAudioDirectory(context: android.content.Context): File {
        val dir = File(context.filesDir, FileConstants.AUDIO_DIRECTORY)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Get or create the transcript directory.
     * @param context Android context
     * @return The transcript directory file
     */
    fun getTranscriptDirectory(context: android.content.Context): File {
        val dir = File(context.filesDir, FileConstants.TRANSCRIPT_DIRECTORY)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Get or create the summary directory.
     * @param context Android context
     * @return The summary directory file
     */
    fun getSummaryDirectory(context: android.content.Context): File {
        val dir = File(context.filesDir, FileConstants.SUMMARY_DIRECTORY)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Generate a unique audio file name with timestamp.
     * Format: audio_yyyyMMdd_HHmmss.m4a
     * @return Generated file name
     */
    fun generateAudioFileName(): String {
        val timestamp = java.text.SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            java.util.Locale.getDefault()
        ).format(java.util.Date())
        return "audio_$timestamp.m4a"
    }
    
    /**
     * Get the size of a file in bytes.
     * @param file The file to check
     * @return File size in bytes, 0 if file doesn't exist
     */
    fun getFileSize(file: File): Long {
        return if (file.exists()) file.length() else 0L
    }
    
    /**
     * Get the size of a file by path.
     * @param path File path
     * @return File size in bytes, 0 if file doesn't exist
     */
    fun getFileSize(path: String): Long {
        return getFileSize(File(path))
    }
    
    /**
     * Delete a file by path.
     * @param path File path to delete
     * @return True if deleted successfully, false otherwise
     */
    fun deleteFile(path: String?): Boolean {
        if (path == null) return false
        val file = File(path)
        return if (file.exists()) file.delete() else true
    }
    
    /**
     * Copy a file from source to destination.
     * @param source Source file
     * @param destination Destination file
     * @return True if copied successfully, false otherwise
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Move a file from source to destination.
     * @param source Source file
     * @param destination Destination file
     * @return True if moved successfully, false otherwise
     */
    fun moveFile(source: File, destination: File): Boolean {
        return try {
            if (copyFile(source, destination)) {
                deleteFile(source.absolutePath)
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Format file size to human-readable string.
     * @param size File size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        
        val df = DecimalFormat("#,##0.#")
        return "${df.format(size / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
    }
    
    /**
     * Check if a file exists.
     * @param path File path
     * @return True if file exists
     */
    fun fileExists(path: String?): Boolean {
        if (path == null) return false
        return File(path).exists()
    }
    
    /**
     * Get file name from path.
     * @param path File path
     * @return File name without extension
     */
    fun getFileName(path: String): String {
        return File(path).nameWithoutExtension
    }
    
    /**
     * Get file extension from path.
     * @param path File path
     * @return File extension (e.g., "m4a", "txt")
     */
    fun getFileExtension(path: String): String {
        return File(path).extension
    }
    
    /**
     * Create a file with parent directories if needed.
     * @param path File path
     * @return True if file was created or already exists
     */
    fun createFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.parentFile != null && !file.parentFile!!.exists()) {
                file.parentFile!!.mkdirs()
            }
            file.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Check if file size exceeds the limit.
     * @param file File to check
     * @return True if file size exceeds 500MB limit
     */
    fun exceedsSizeLimit(file: File): Boolean {
        return getFileSize(file) >= FileConstants.MAX_FILE_SIZE_BYTES
    }
    
    /**
     * Check if file size exceeds the limit by path.
     * @param path File path
     * @return True if file size exceeds 500MB limit
     */
    fun exceedsSizeLimit(path: String): Boolean {
        return exceedsSizeLimit(File(path))
    }
}
