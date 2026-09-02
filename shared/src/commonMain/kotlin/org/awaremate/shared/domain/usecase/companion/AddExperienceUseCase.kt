package org.awaremate.shared.domain.usecase.companion

import kotlinx.coroutines.flow.firstOrNull
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionEmotionStateMachine
import org.awaremate.shared.domain.model.CompanionEvent
import org.awaremate.shared.domain.model.CompanionGrowthRules
import org.awaremate.shared.domain.model.MomentumCalculator
import org.awaremate.shared.domain.repository.CompanionRepository

data class ExperienceResult(
    val updatedCompanion: Companion,
    val earnedXp: Int,
    val category: CompanionCategory,
    val isEvolution: Boolean
)

class AddExperienceUseCase(
    private val companionRepository: CompanionRepository
) {
    suspend operator fun invoke(
        category: CompanionCategory,
        amount: Int,
        baseMomentumBoost: Double = MomentumCalculator.BASE_ACTIVITY_BOOST,
        daysInactive: Int = 0
    ): Result<ExperienceResult> = runCatching {
        require(amount >= 0) { "Experience amount cannot be negative" }

        val current = companionRepository.getCompanion().firstOrNull() ?: Companion()
        val oldXp = current.experiencePoints

        val updatedHappiness = current.happinessXp + if (category == CompanionCategory.HAPPINESS) amount else 0
        val updatedEnergy = current.energyXp + if (category == CompanionCategory.ENERGY) amount else 0
        val updatedWisdom = current.wisdomXp + if (category == CompanionCategory.WISDOM) amount else 0
        val updatedCreativity = current.creativityXp + if (category == CompanionCategory.CREATIVITY) amount else 0
        val newTotalXp = updatedHappiness + updatedEnergy + updatedWisdom + updatedCreativity

        val newStage = CompanionGrowthRules.getStageForXp(newTotalXp)
        val isEvolution = CompanionGrowthRules.isStageEvolution(oldXp, newTotalXp)

        val updatedMomentum = MomentumCalculator.calculateBoostedScore(
            currentScore = current.momentumScore,
            baseBoost = baseMomentumBoost,
            daysInactive = daysInactive
        )

        val emotionEvent = if (isEvolution) {
            CompanionEvent.Evolved(newStage)
        } else {
            CompanionEvent.ChallengeCompleted(category)
        }
        val updatedEmotion = CompanionEmotionStateMachine.transition(current.emotion, emotionEvent)

        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

        val updatedCompanion = current.copy(
            stage = newStage,
            emotion = updatedEmotion,
            experiencePoints = newTotalXp,
            momentumScore = updatedMomentum,
            happinessXp = updatedHappiness,
            energyXp = updatedEnergy,
            wisdomXp = updatedWisdom,
            creativityXp = updatedCreativity,
            lastUpdatedEpochMs = now
        )

        companionRepository.saveCompanion(updatedCompanion).getOrThrow()

        ExperienceResult(
            updatedCompanion = updatedCompanion,
            earnedXp = amount,
            category = category,
            isEvolution = isEvolution
        )
    }
}
