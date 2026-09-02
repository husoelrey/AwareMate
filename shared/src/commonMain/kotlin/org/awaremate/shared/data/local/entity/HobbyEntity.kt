package org.awaremate.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCategory
import org.awaremate.shared.domain.model.HobbyEnergyLevel

@Entity(tableName = "hobbies")
data class HobbyEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val beginnerTip: String,
    val estimatedDurationMinutes: Int,
    val energyLevel: String,
    val isBookmarked: Boolean = false,
    val sessionsCompleted: Int = 0,
    val lastCompletedEpochMs: Long? = null,
    val tags: List<String> = emptyList()
) {
    fun toDomain(): Hobby = Hobby(
        id = id,
        title = title,
        category = runCatching { HobbyCategory.valueOf(category) }.getOrDefault(HobbyCategory.CREATIVE_ARTS),
        description = description,
        beginnerTip = beginnerTip,
        estimatedDurationMinutes = estimatedDurationMinutes,
        energyLevel = runCatching { HobbyEnergyLevel.valueOf(energyLevel) }.getOrDefault(HobbyEnergyLevel.MODERATE),
        isBookmarked = isBookmarked,
        sessionsCompleted = sessionsCompleted,
        lastCompletedEpochMs = lastCompletedEpochMs,
        tags = tags
    )

    companion object {
        fun fromDomain(hobby: Hobby): HobbyEntity = HobbyEntity(
            id = hobby.id,
            title = hobby.title,
            category = hobby.category.name,
            description = hobby.description,
            beginnerTip = hobby.beginnerTip,
            estimatedDurationMinutes = hobby.estimatedDurationMinutes,
            energyLevel = hobby.energyLevel.name,
            isBookmarked = hobby.isBookmarked,
            sessionsCompleted = hobby.sessionsCompleted,
            lastCompletedEpochMs = hobby.lastCompletedEpochMs,
            tags = hobby.tags
        )
    }
}
