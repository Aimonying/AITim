package com.tingjizhushou.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a recording/transcription record.
 * 
 * @property id Auto-generated primary key
 * @property title Display title of the recording
 * @property type Recording type: "REALTIME" (real-time recording), "UPLOAD" (file upload), "TEXT" (text input)
 * @property createdAt Creation timestamp in milliseconds
 * @property audioFilePath Path to the audio file (null for TEXT type)
 * @property audioSize Size of the audio file in bytes
 * @property duration Recording duration in seconds
 * @property transcriptText The transcribed text content
 * @property meetingMinutes Generated meeting minutes summary
 * @property language Language code for transcription (e.g., "zh-CN")
 * @property isFavorite Whether the record is marked as favorite
 * @property tags Comma-separated tags for categorization
 * @property updatedAt Last update timestamp
 */
@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String,
    val createdAt: Long,
    val audioFilePath: String? = null,
    val audioSize: Long = 0,
    val duration: Int = 0,
    val transcriptText: String? = null,
    val meetingMinutes: String? = null,
    val language: String = "zh-CN",
    val isFavorite: Boolean = false,
    val tags: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_REALTIME = "REALTIME"
        const val TYPE_UPLOAD = "UPLOAD"
        const val TYPE_TEXT = "TEXT"
    }
}
