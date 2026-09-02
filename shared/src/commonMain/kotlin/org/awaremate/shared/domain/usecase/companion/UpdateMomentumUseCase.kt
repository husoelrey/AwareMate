package org.awaremate.shared.domain.usecase.companion

import kotlinx.coroutines.flow.firstOrNull
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionEmotionStateMachine
import org.awaremate.shared.domain.model.CompanionEvent
import org.awaremate.shared.domain.model.MomentumCalculator
import org.awaremate.shared.domain.repository.CompanionRepository

class UpdateMomentumUseCase(
    private val companionRepository: CompanionRepository
) {
    /**
     * Applies gradual decay based on the number of days inactive.
     * Guaranteed non-punitive: score gracefully decreases without abrupt zero resets.
     */
    suspend fun applyInactivityDecay(daysInactive: Int): Result<Companion> = runCatching {
        if (daysInactive <= 0) {
            return@runCatching companionRepository.getCompanion().firstOrNull() ?: Companion()
        }

        val current = companionRepository.getCompanion().firstOrNull() ?: Companion()
        val decayedScore = MomentumCalculator.calculateDecayedScore(current.momentumScore, daysInactive)

        val newEmotion = CompanionEmotionStateMachine.transition(
            currentEmotion = current.emotion,
            event = CompanionEvent.InactivityDecay(daysInactive)
        )

        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val updated = current.copy(
            momentumScore = decayedScore,
            emotion = newEmotion,
            lastUpdatedEpochMs = now
        )

        companionRepository.saveCompanion(updated).getOrThrow()
        updated
    }

    /**
     * Boosts momentum when the user completes an awareness activity.
     */
    suspend fun boostMomentum(
        baseBoost: Double = MomentumCalculator.BASE_ACTIVITY_BOOST,
        daysInactive: Int = 0
    ): Result<Companion> = runCatching {
        val current = companionRepository.getCompanion().firstOrNull() ?: Companion()
        val boostedScore = MomentumCalculator.calculateBoostedScore(
            currentScore = current.momentumScore,
            baseBoost = baseBoost,
            daysInactive = daysInactive
        )

        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val updated = current.copy(
            momentumScore = boostedScore,
            lastUpdatedEpochMs = now
        )

        companionRepository.saveCompanion(updated).getOrThrow()
        updated
    }
}
