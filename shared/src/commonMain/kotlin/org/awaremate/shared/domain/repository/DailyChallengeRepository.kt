package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.DailyChallenge

interface DailyChallengeRepository {
    fun getChallengesForDate(dateString: String): Flow<List<DailyChallenge>>
    suspend fun saveChallenges(challenges: List<DailyChallenge>): Result<Unit>
    suspend fun completeChallenge(id: String): Result<Unit>
}
