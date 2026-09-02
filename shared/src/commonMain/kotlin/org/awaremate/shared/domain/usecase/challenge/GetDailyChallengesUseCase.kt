package org.awaremate.shared.domain.usecase.challenge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.awaremate.shared.domain.model.ChallengeCatalog
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.repository.DailyChallengeRepository

class GetDailyChallengesUseCase(
    private val dailyChallengeRepository: DailyChallengeRepository
) {
    /**
     * Observes challenges for a specific [dateString].
     */
    operator fun invoke(dateString: String): Flow<List<DailyChallenge>> {
        return dailyChallengeRepository.getChallengesForDate(dateString)
    }

    /**
     * Retrieves existing challenges for [dateString], or generates and saves a new set if none exist.
     */
    suspend fun getOrGenerateChallenges(
        dateString: String,
        userId: String = "primary"
    ): Result<List<DailyChallenge>> = runCatching {
        val existing = dailyChallengeRepository.getChallengesForDate(dateString).first()
        if (existing.isNotEmpty()) {
            existing
        } else {
            val generated = ChallengeCatalog.generateDailyChallenges(dateString = dateString, userId = userId)
            dailyChallengeRepository.saveChallenges(generated).getOrThrow()
            generated
        }
    }
}
