package org.awaremate.shared.domain.usecase.challenge

import org.awaremate.shared.domain.model.ChallengeCatalog
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.repository.DailyChallengeRepository

class GenerateDailyChallengesUseCase(
    private val dailyChallengeRepository: DailyChallengeRepository
) {
    suspend operator fun invoke(
        dateString: String,
        userId: String = "primary"
    ): Result<List<DailyChallenge>> = runCatching {
        val challenges = ChallengeCatalog.generateDailyChallenges(dateString = dateString, userId = userId)
        dailyChallengeRepository.saveChallenges(challenges).getOrThrow()
        challenges
    }
}
