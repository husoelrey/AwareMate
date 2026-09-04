package org.awaremate.shared.presentation.growth.components

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.SelfDiscoveryPrompt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TodaysFeelingCalendarTest {
    @Test
    fun februaryLeapYearBuildsCompleteMondayFirstGrid() {
        val days = buildMoodCalendarDays(
            year = 2024,
            monthNumber = 2,
            moodEntries = emptyList(),
            prompts = emptyList(),
            timeZone = TimeZone.UTC
        )

        assertEquals(35, days.size)
        assertEquals(4, days.indexOfFirst { it.date?.dayOfMonth == 1 } + 1)
        assertEquals(29, days.count { it.date != null })
        assertTrue(days.filter { it.date != null }.all { it.moodEntry == null })
    }

    @Test
    fun dayUsesLatestMoodAndIncludesSameDayReflection() {
        val noon = Instant.parse("2026-09-04T12:00:00Z").toEpochMilliseconds()
        val evening = Instant.parse("2026-09-04T18:00:00Z").toEpochMilliseconds()
        val moods = listOf(
            MoodEntry("early", "user", noon, "🌿", 3, 2),
            MoodEntry("latest", "user", evening, "😊", 4, 5, note = "A steady day")
        )
        val prompt = SelfDiscoveryPrompt(
            id = "prompt",
            category = "habits",
            question = "What did you notice?",
            curiosityHint = "Pause and observe",
            isAcknowledged = true,
            userReflection = "I reached for my phone while waiting.",
            lastAnsweredEpochMs = evening
        )

        val day = buildMoodCalendarDays(2026, 9, moods, listOf(prompt), TimeZone.UTC)
            .first { it.date?.dayOfMonth == 4 }

        assertEquals("latest", day.moodEntry?.id)
        assertEquals(listOf(prompt), day.selfDiscoveryAnswers)
    }

    @Test
    fun blankDayHasNoMoodOrPunitiveSentinel() {
        val blank = buildMoodCalendarDays(2026, 9, emptyList(), emptyList(), TimeZone.UTC)
            .first { it.date?.dayOfMonth == 12 }

        assertNull(blank.moodEntry)
        assertTrue(blank.selfDiscoveryAnswers.isEmpty())
    }
}
