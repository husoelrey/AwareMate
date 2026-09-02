package org.awaremate.shared.presentation.companion

import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.usecase.companion.CompanionGrowthMetrics

data class CompanionState(
    val companion: Companion = Companion(),
    val growthMetrics: CompanionGrowthMetrics? = null,
    val isLoading: Boolean = false,
    val interactionMessage: String? = null
)

sealed interface CompanionIntent {
    data object LoadCompanion : CompanionIntent
    data class WaterCompanion(val category: CompanionCategory = CompanionCategory.HAPPINESS) : CompanionIntent
    data class SetEmotion(val emotion: CompanionEmotion) : CompanionIntent
    data class RenameCompanion(val newName: String) : CompanionIntent
    data object ClearInteractionMessage : CompanionIntent
}
