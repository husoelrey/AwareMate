package org.awaremate.shared.domain.usecase.growth

import org.awaremate.shared.domain.model.MoodEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWeeklyMoodInsightsUseCaseTest {

    private val useCase = GetWeeklyMoodInsightsUseCase()

    @Test
    fun testEmptyEntriesReturnsDefaultFriendlyInsights() {
        val insights = useCase(emptyList())

        assertEquals(0, insights.totalCheckIns)
        assertEquals(0.0, insights.averageMoodScore)
        assertEquals("🌱", insights.dominantEmoji)
        assertEquals(7, insights.dailyMoodPoints.size)
        assertTrue(insights.compassionateInsight.contains("Start logging"))
    }

    @Test
    fun testCalculatesAverageScoresAndDominantEmoji() {
        val entries = listOf(
            MoodEntry(
                id = "m1",
                userId = "u1",
                timestampEpochMs = 1725280000000L,
                emoji = "😊",
                moodScore = 4,
                energyLevel = 4
            ),
            MoodEntry(
                id = "m2",
                userId = "u1",
                timestampEpochMs = 1725281000000L,
                emoji = "😊",
                moodScore = 4,
                energyLevel = 3
            ),
            MoodEntry(
                id = "m3",
                userId = "u1",
                timestampEpochMs = 1725282000000L,
                emoji = "😄",
                moodScore = 5,
                energyLevel = 5
            )
        )

        val insights = useCase(entries, completedChallengesThisWeek = 4, mindfulBreathingMinutes = 8)

        assertEquals(3, insights.totalCheckIns)
        assertEquals(4.3, insights.averageMoodScore)
        assertEquals(4.0, insights.averageEnergyLevel)
        assertEquals("😊", insights.dominantEmoji)
        assertEquals("Content & Centered", insights.dominantMoodLabel)
        assertEquals(4, insights.completedChallengesThisWeek)
        assertEquals(8, insights.mindfulBreathingMinutes)
        assertTrue(insights.compassionateInsight.isNotEmpty())
    }
}
