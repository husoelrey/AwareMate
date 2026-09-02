package org.awaremate.shared.domain.usecase.companion

import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionGrowthRules
import org.awaremate.shared.domain.model.CompanionStage

data class CompanionGrowthMetrics(
    val stage: CompanionStage,
    val totalXp: Int,
    val progressWithinStage: Float,
    val remainingXpForNextStage: Int,
    val nextStage: CompanionStage?,
    val isMaxStage: Boolean
)

class CalculateGrowthStageUseCase {
    operator fun invoke(totalXp: Int): CompanionGrowthMetrics {
        val stage = CompanionGrowthRules.getStageForXp(totalXp)
        val progress = CompanionGrowthRules.getProgressWithinStage(totalXp)
        val remaining = CompanionGrowthRules.getRemainingXpForNextStage(totalXp)
        val next = CompanionGrowthRules.getNextStage(stage)

        return CompanionGrowthMetrics(
            stage = stage,
            totalXp = totalXp,
            progressWithinStage = progress,
            remainingXpForNextStage = remaining,
            nextStage = next,
            isMaxStage = next == null
        )
    }

    operator fun invoke(companion: Companion): CompanionGrowthMetrics {
        return invoke(companion.experiencePoints)
    }
}
