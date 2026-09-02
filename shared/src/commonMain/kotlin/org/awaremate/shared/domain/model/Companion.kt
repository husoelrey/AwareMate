package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CompanionStage {
    SEED,
    SPROUT,
    BLOOM,
    TREE,
    ANCIENT_TREE
}

@Serializable
enum class CompanionEmotion {
    PEACEFUL,
    CURIOUS,
    CHEERFUL,
    TIRED,
    RESTING
}

@Serializable
enum class CompanionCategory {
    HAPPINESS,
    ENERGY,
    WISDOM,
    CREATIVITY
}

@Serializable
data class Companion(
    val id: String = "primary",
    val name: String = "Sprout",
    val stage: CompanionStage = CompanionStage.SEED,
    val emotion: CompanionEmotion = CompanionEmotion.PEACEFUL,
    val experiencePoints: Int = 0,
    val momentumScore: Double = 100.0,
    val happinessXp: Int = 0,
    val energyXp: Int = 0,
    val wisdomXp: Int = 0,
    val creativityXp: Int = 0,
    val lastUpdatedEpochMs: Long = 0L
)
