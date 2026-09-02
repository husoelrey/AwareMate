package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CompanionStage {
    SEED,
    SPROUT,
    SAPLING,
    BLOOMING_TREE,
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
data class Companion(
    val id: String,
    val name: String,
    val stage: CompanionStage = CompanionStage.SEED,
    val emotion: CompanionEmotion = CompanionEmotion.PEACEFUL,
    val experiencePoints: Int = 0,
    val momentumScore: Double = 1.0
)
