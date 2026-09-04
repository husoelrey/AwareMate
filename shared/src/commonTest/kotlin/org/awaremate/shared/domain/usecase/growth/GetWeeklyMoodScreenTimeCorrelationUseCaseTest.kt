package org.awaremate.shared.domain.usecase.growth

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.awaremate.shared.domain.model.DailyUsageSummary
import org.awaremate.shared.domain.model.MoodEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetWeeklyMoodScreenTimeCorrelationUseCaseTest {
    private val useCase = GetWeeklyMoodScreenTimeCorrelationUseCase()
    private val currentWeekEnd = Instant.parse("2026-09-06T20:00:00Z").toEpochMilliseconds()

    @Test
    fun fewerThanFiveDistinctMoodDaysNeverProducesChartData() {
        val moods = (1..3).map { day -> mood(day, energy = 3) }
        val result = useCase(moods, usage(1..5), currentWeekEnd, TimeZone.UTC)

        assertFalse(result.hasEnoughMoodDays)
        assertTrue(result.points.isEmpty())
        assertEquals(null, result.observationalInsight)
    }

    @Test
    fun fiveMoodDaysProducesCurrentWeekOverlayAndObservationalText() {
        val moods = (1..5).map { day -> mood(day, energy = 6 - day) }
        val result = useCase(moods, usage(1..5), currentWeekEnd, TimeZone.UTC)

        assertTrue(result.hasEnoughMoodDays)
        assertEquals(5, result.points.size)
        assertNotNull(result.observationalInsight)
        assertFalse(result.observationalInsight.contains("cause", ignoreCase = true))
        assertFalse(result.observationalInsight.contains("because", ignoreCase = true))
        assertFalse(result.observationalInsight.contains("due to", ignoreCase = true))
    }

    @Test
    fun duplicateCheckInsCountAsOneMoodDay() {
        val moods = listOf(mood(1, 2), mood(1, 4), mood(2, 3), mood(3, 3), mood(4, 3))
        assertFalse(useCase.hasEnoughMoodDays(moods, currentWeekEnd, TimeZone.UTC))
    }

    private fun mood(day: Int, energy: Int): MoodEntry = MoodEntry(
        id = "mood-$day-$energy",
        userId = "user",
        timestampEpochMs = Instant.parse("2026-09-0${day}T12:00:00Z").toEpochMilliseconds(),
        emoji = "🌿",
        moodScore = 3,
        energyLevel = energy
    )

    private fun usage(days: IntRange): List<DailyUsageSummary> = days.map { day ->
        DailyUsageSummary(
            dateString = "2026-09-0$day",
            totalScreenTimeMs = day * 60L * 60L * 1000L
        )
    }
}
