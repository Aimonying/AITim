package com.tingjizhushou.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a transcript segment.
 * Each record can have multiple transcript segments.
 * 
 * @property id Auto-generated primary key
 * @property recordId Foreign key to the parent record
 * @property content The transcribed text content
 * @property language Language code used for this transcription
 * @property engine Transcription engine: "ONLINE" or "OFFLINE"
 * @property confidence Confidence score of the transcription (0.0 - 1.0)
 * @property createdAt Creation timestamp in milliseconds
 */
@Entity(
    tableName = "transcripts",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recordId"])]
)
data class TranscriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordId: Long,
    val content: String,
    val language: String,
    val engine: String,
    val confidence: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val ENGINE_ONLINE = "ONLINE"
        const val ENGINE_OFFLINE = "OFFLINE"
    }
}
