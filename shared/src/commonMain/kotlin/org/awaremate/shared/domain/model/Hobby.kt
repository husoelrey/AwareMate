package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class HobbyCategory {
    CREATIVE_ARTS,
    NATURE_OUTDOORS,
    MINDFUL_LIFESTYLE,
    HANDS_ON_CRAFT,
    MUSIC_LITERATURE
}

@Serializable
enum class HobbyEnergyLevel {
    GENTLE,   // Low physical/mental energy required (reading, tea ritual, sketching)
    MODERATE, // Medium energy (baking, light gardening, casual walking)
    ACTIVE    // High energy / active movement (cycling, hiking, dancing)
}

@Serializable
data class Hobby(
    val id: String,
    val title: String,
    val category: HobbyCategory,
    val description: String,
    val beginnerTip: String,
    val estimatedDurationMinutes: Int = 30,
    val energyLevel: HobbyEnergyLevel = HobbyEnergyLevel.MODERATE,
    val isBookmarked: Boolean = false,
    val sessionsCompleted: Int = 0,
    val lastCompletedEpochMs: Long? = null,
    val tags: List<String> = emptyList()
)
