package org.awaremate.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.awaremate.shared.domain.model.Companion as DomainCompanion
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage

@Entity(tableName = "companions")
data class CompanionEntity(
    @PrimaryKey
    val id: String = "primary",
    val name: String = "Sprout",
    val stage: String = CompanionStage.SEED.name,
    val emotion: String = CompanionEmotion.PEACEFUL.name,
    val experiencePoints: Int = 0,
    val momentumScore: Double = 1.0,
    val happinessXp: Int = 0,
    val energyXp: Int = 0,
    val wisdomXp: Int = 0,
    val creativityXp: Int = 0,
    val lastUpdatedEpochMs: Long = 0L
) {
    fun toDomain(): DomainCompanion = DomainCompanion(
        id = id,
        name = name,
        stage = runCatching { CompanionStage.valueOf(stage) }.getOrDefault(CompanionStage.SEED),
        emotion = runCatching { CompanionEmotion.valueOf(emotion) }.getOrDefault(CompanionEmotion.PEACEFUL),
        experiencePoints = experiencePoints,
        momentumScore = momentumScore,
        happinessXp = happinessXp,
        energyXp = energyXp,
        wisdomXp = wisdomXp,
        creativityXp = creativityXp,
        lastUpdatedEpochMs = lastUpdatedEpochMs
    )

    companion object Factory {
        fun fromDomain(companion: DomainCompanion): CompanionEntity = CompanionEntity(
            id = companion.id,
            name = companion.name,
            stage = companion.stage.name,
            emotion = companion.emotion.name,
            experiencePoints = companion.experiencePoints,
            momentumScore = companion.momentumScore,
            happinessXp = companion.happinessXp,
            energyXp = companion.energyXp,
            wisdomXp = companion.wisdomXp,
            creativityXp = companion.creativityXp,
            lastUpdatedEpochMs = companion.lastUpdatedEpochMs
        )
    }
}
