package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class FocusCategory {
    STUDY,
    MINDFULNESS,
    DEEP_WORK,
    OFFLINE_HOBBY,
    OTHER
}

@Serializable
data class FocusSession(
    val id: String,
    val userId: String,
    val startTimeEpochMs: Long,
    val durationSeconds: Int,
    val category: FocusCategory = FocusCategory.DEEP_WORK,
    val earnedXp: Int = 0,
    val completed: Boolean = true,
    val note: String? = null,
    val isSynced: Boolean = false
)
