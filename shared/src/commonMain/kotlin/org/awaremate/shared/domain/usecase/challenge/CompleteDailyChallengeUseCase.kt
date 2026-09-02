package org.awaremate.shared.domain.usecase.challenge

import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.MomentumCalculator
import org.awaremate.shared.domain.repository.DailyChallengeRepository
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.ExperienceResult

class CompleteDailyChallengeUseCase(
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val addExperienceUseCase: AddExperienceUseCase
) {
    suspend operator fun invoke(
        challenge: DailyChallenge,
        daysInactive: Int = 0
    ): Result<ExperienceResult> = runCatching {
        // Mark as completed locally and queue for sync
        dailyChallengeRepository.completeChallenge(challenge.id).getOrThrow()

        // Award XP, boost momentum, and transition companion emotion
        val result = addExperienceUseCase(
            category = challenge.category,
            amount = challenge.xpReward,
            baseMomentumBoost = MomentumCalculator.CHALLENGE_COMPLETION_BOOST,
            daysInactive = daysInactive
        ).getOrThrow()

        result
    }
}
