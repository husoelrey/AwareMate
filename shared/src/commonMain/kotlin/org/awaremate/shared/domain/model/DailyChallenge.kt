package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyChallenge(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: CompanionCategory = CompanionCategory.WISDOM,
    val xpReward: Int = 20,
    val dateString: String,
    val completed: Boolean = false,
    val completedAtEpochMs: Long? = null,
    val isSynced: Boolean = false
)
