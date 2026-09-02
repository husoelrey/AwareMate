package org.awaremate.shared.domain.usecase.awareness

import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.local.entity.DailyChallengeEntity
import org.awaremate.shared.data.local.entity.FocusSessionEntity
import org.awaremate.shared.data.local.entity.MoodEntryEntity
import org.awaremate.shared.data.repository.DailyChallengeRepositoryImpl
import org.awaremate.shared.data.repository.FocusSessionRepositoryImpl
import org.awaremate.shared.data.repository.MoodRepositoryImpl
import org.awaremate.shared.domain.model.AwarenessTier
import org.awaremate.shared.domain.model.UserPreferences
import org.awaremate.shared.test.FakeDailyChallengeDao
import org.awaremate.shared.test.FakeFocusSessionDao
import org.awaremate.shared.test.FakeMoodEntryDao
import org.awaremate.shared.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculateAwarenessScoreUseCaseTest {

    private val useCase = CalculateAwarenessScoreUseCase()

    @Test
    fun testPerfectAwarenessScore() {
        val score = useCase(
            screenTimeMinutes = 120,
            targetScreenTimeGoalMinutes = 180, // within goal -> 30 pts
            focusSessionMinutes = 30,         // capped at 25 pts
            moodEntriesCount = 2,             // 25 pts
            completedChallengesCount = 3      // 20 pts
        )

        assertEquals(100, score.totalScore)
        assertEquals(30, score.digitalBalanceScore)
        assertEquals(25, score.mindfulnessScore)
        assertEquals(25, score.emotionalScore)
        assertEquals(20, score.growthScore)
        assertEquals(AwarenessTier.MINDFUL_MASTER, score.tier)
    }

    @Test
    fun testZeroActivityAwarenessScore() {
        val score = useCase(
            screenTimeMinutes = 400,
            targetScreenTimeGoalMinutes = 180,
            focusSessionMinutes = 0,
            moodEntriesCount = 0,
            completedChallengesCount = 0
        )

        assertEquals(0, score.totalScore)
        assertEquals(AwarenessTier.GENTLE_START, score.tier)
    }

    @Test
    fun testModerateBalancedAwarenessScore() {
        val score = useCase(
            screenTimeMinutes = 180,
            targetScreenTimeGoalMinutes = 180, // exact goal -> 30 pts
            focusSessionMinutes = 15,         // 15 pts
            moodEntriesCount = 1,             // 20 pts
            completedChallengesCount = 1      // 10 pts
        )

        // 30 + 15 + 20 + 10 = 75
        assertEquals(75, score.totalScore)
        assertEquals(AwarenessTier.BALANCED_EXPLORER, score.tier)
    }

    @Test
    fun testCalculateForDateWithRepositories() = runTest {
        val moodDao = FakeMoodEntryDao()
        val moodRepo = MoodRepositoryImpl(moodDao)
        val focusDao = FakeFocusSessionDao()
        val focusRepo = FocusSessionRepositoryImpl(focusDao)
        val challengeDao = FakeDailyChallengeDao()
        val challengeRepo = DailyChallengeRepositoryImpl(challengeDao)
        val prefsRepo = FakePreferencesRepository(UserPreferences(dailyScreenTimeGoalMinutes = 120))

        val fullUseCase = CalculateAwarenessScoreUseCase(
            moodRepository = moodRepo,
            focusSessionRepository = focusRepo,
            dailyChallengeRepository = challengeRepo,
            preferencesRepository = prefsRepo
        )

        val dateString = "2026-09-02"
        val startEpoch = 1000L
        val endEpoch = 5000L

        // Insert 1 mood entry
        moodDao.insertMoodEntry(
            MoodEntryEntity(
                id = "m-1",
                userId = "u-1",
                timestampEpochMs = 2000L,
                emoji = "🌱",
                moodScore = 4,
                energyLevel = 3
            )
        )

        // Insert 1 focus session of 20 minutes (1200 seconds)
        focusDao.insertSession(
            FocusSessionEntity(
                id = "fs-1",
                userId = "u-1",
                startTimeEpochMs = 2500L,
                durationSeconds = 1200,
                completed = true
            )
        )

        // Insert 1 completed challenge
        challengeDao.insertChallenge(
            DailyChallengeEntity(
                id = "dc-1",
                userId = "u-1",
                title = "Hydrate",
                description = "Drink water",
                dateString = dateString,
                completed = true
            )
        )

        val score = fullUseCase.calculateForDate(
            dateString = dateString,
            screenTimeMinutes = 100, // within 120 goal -> 30 pts
            startOfDayEpochMs = startEpoch,
            endOfDayEpochMs = endEpoch
        )

        // Digital (30) + Focus (20) + Mood (20) + Challenge (10) = 80
        assertEquals(80, score.totalScore)
        assertEquals(AwarenessTier.MINDFUL_MASTER, score.tier)
    }
}
