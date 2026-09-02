package org.awaremate.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.DailyChallenge

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: String = CompanionCategory.WISDOM.name,
    val xpReward: Int = 20,
    val dateString: String,
    val completed: Boolean = false,
    val completedAtEpochMs: Long? = null,
    val isSynced: Boolean = false
) {
    fun toDomain(): DailyChallenge = DailyChallenge(
        id = id,
        userId = userId,
        title = title,
        description = description,
        category = runCatching { CompanionCategory.valueOf(category) }.getOrDefault(CompanionCategory.WISDOM),
        xpReward = xpReward,
        dateString = dateString,
        completed = completed,
        completedAtEpochMs = completedAtEpochMs,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(challenge: DailyChallenge): DailyChallengeEntity = DailyChallengeEntity(
            id = challenge.id,
            userId = challenge.userId,
            title = challenge.title,
            description = challenge.description,
            category = challenge.category.name,
            xpReward = challenge.xpReward,
            dateString = challenge.dateString,
            completed = challenge.completed,
            completedAtEpochMs = challenge.completedAtEpochMs,
            isSynced = challenge.isSynced
        )
    }
}
