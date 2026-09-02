package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MoodEntry(
    val id: String,
    val userId: String,
    val timestampEpochMs: Long,
    val emoji: String,
    val moodScore: Int,
    val energyLevel: Int,
    val note: String? = null,
    val tags: List<String> = emptyList(),
    val isSynced: Boolean = false
)
