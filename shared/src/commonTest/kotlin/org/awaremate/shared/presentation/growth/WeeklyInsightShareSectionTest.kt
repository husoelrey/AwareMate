package org.awaremate.shared.presentation.growth

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.presentation.growth.components.buildWeeklyMoodStrip
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WeeklyInsightShareSectionTest {

    @Test
    fun stripUsesMondayToSundayAndLatestMoodForEachDay() {
        val zone = TimeZone.UTC
        val wednesday = LocalDate(2026, 9, 2).atStartOfDayIn(zone).toEpochMilliseconds()
        val entries = listOf(
            MoodEntry(id = "early", userId = "u", timestampEpochMs = wednesday, emoji = "🌿", moodScore = 3, energyLevel = 2),
            MoodEntry(id = "late", userId = "u", timestampEpochMs = wednesday + 5_000, emoji = "😊", moodScore = 4, energyLevel = 4)
        )

        val days = buildWeeklyMoodStrip(entries, wednesday, zone)

        assertEquals(LocalDate(2026, 8, 31), days.first().date)
        assertEquals(LocalDate(2026, 9, 6), days.last().date)
        assertEquals("Mon", days.first().dayLabel)
        assertEquals("late", days[2].moodEntry?.id)
        assertNull(days[0].moodEntry)
    }
}
