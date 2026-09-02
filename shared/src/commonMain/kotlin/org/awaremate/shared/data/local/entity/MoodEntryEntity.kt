package org.awaremate.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.awaremate.shared.domain.model.MoodEntry

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val timestampEpochMs: Long,
    val emoji: String,
    val moodScore: Int,
    val energyLevel: Int,
    val note: String? = null,
    val tags: List<String> = emptyList(),
    val isSynced: Boolean = false
) {
    fun toDomain(): MoodEntry = MoodEntry(
        id = id,
        userId = userId,
        timestampEpochMs = timestampEpochMs,
        emoji = emoji,
        moodScore = moodScore,
        energyLevel = energyLevel,
        note = note,
        tags = tags,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(entry: MoodEntry): MoodEntryEntity = MoodEntryEntity(
            id = entry.id,
            userId = entry.userId,
            timestampEpochMs = entry.timestampEpochMs,
            emoji = entry.emoji,
            moodScore = entry.moodScore,
            energyLevel = entry.energyLevel,
            note = entry.note,
            tags = entry.tags,
            isSynced = entry.isSynced
        )
    }
}
