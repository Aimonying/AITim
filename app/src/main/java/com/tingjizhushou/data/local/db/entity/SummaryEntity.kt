package com.tingjizhushou.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a meeting summary/minutes.
 * Each record can have one summary.
 * 
 * @property id Auto-generated primary key
 * @property recordId Foreign key to the parent record
 * @property title Meeting title (optional)
 * @property location Meeting location (optional)
 * @property participants Comma-separated list of participants (optional)
 * @property agenda Meeting agenda items (optional)
 * @property conclusion Meeting conclusions/decisions (optional)
 * @property rawText Raw summary text (optional)
 * @property createdAt Creation timestamp in milliseconds
 * @property updatedAt Last update timestamp in milliseconds
 */
@Entity(
    tableName = "summaries",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recordId"], unique = true)]
)
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordId: Long,
    val title: String? = null,
    val location: String? = null,
    val participants: String? = null,
    val agenda: String? = null,
    val conclusion: String? = null,
    val rawText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
