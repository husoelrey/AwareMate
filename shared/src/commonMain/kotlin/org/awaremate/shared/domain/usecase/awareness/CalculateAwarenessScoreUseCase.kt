package org.awaremate.shared.domain.usecase.awareness

import kotlinx.coroutines.flow.first
import org.awaremate.shared.domain.model.AwarenessScore
import org.awaremate.shared.domain.model.AwarenessScoreCalculator
import org.awaremate.shared.domain.repository.DailyChallengeRepository
import org.awaremate.shared.domain.repository.FocusSessionRepository
import org.awaremate.shared.domain.repository.MoodRepository
import org.awaremate.shared.domain.repository.PreferencesRepository

class CalculateAwarenessScoreUseCase(
    private val moodRepository: MoodRepository? = null,
    private val focusSessionRepository: FocusSessionRepository? = null,
    private val dailyChallengeRepository: DailyChallengeRepository? = null,
    private val preferencesRepository: PreferencesRepository? = null
) {
    /**
     * Pure calculation method using explicit metric parameters.
     */
    operator fun invoke(
        screenTimeMinutes: Int,
        targetScreenTimeGoalMinutes: Int,
        focusSessionMinutes: Int,
        moodEntriesCount: Int,
        completedChallengesCount: Int
    ): AwarenessScore {
        return AwarenessScoreCalculator.calculate(
            screenTimeMinutes = screenTimeMinutes,
            targetScreenTimeGoalMinutes = targetScreenTimeGoalMinutes,
            focusSessionMinutes = focusSessionMinutes,
            moodEntriesCount = moodEntriesCount,
            completedChallengesCount = completedChallengesCount
        )
    }

    /**
     * Aggregates metrics for a specific date and computes the composite awareness score.
     */
    suspend fun calculateForDate(
        dateString: String,
        screenTimeMinutes: Int,
        startOfDayEpochMs: Long,
        endOfDayEpochMs: Long
    ): AwarenessScore {
        val targetGoalMinutes = preferencesRepository?.getPreferences()?.first()?.dailyScreenTimeGoalMinutes ?: 180

        val moodCount = moodRepository?.getMoodEntriesForRange(startOfDayEpochMs, endOfDayEpochMs)?.first()?.size ?: 0

        val focusSessions = focusSessionRepository?.getRecentSessions(100)?.first() ?: emptyList()
        val totalFocusMinutes = focusSessions
            .filter { it.startTimeEpochMs in startOfDayEpochMs..endOfDayEpochMs }
            .sumOf { it.durationSeconds } / 60

        val challenges = dailyChallengeRepository?.getChallengesForDate(dateString)?.first() ?: emptyList()
        val completedChallengesCount = challenges.count { it.completed }

        return invoke(
            screenTimeMinutes = screenTimeMinutes,
            targetScreenTimeGoalMinutes = targetGoalMinutes,
            focusSessionMinutes = totalFocusMinutes,
            moodEntriesCount = moodCount,
            completedChallengesCount = completedChallengesCount
        )
    }
}
