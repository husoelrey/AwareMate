package org.awaremate.shared.domain.usecase.growth

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.awaremate.shared.domain.model.DailyMoodPoint
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.WeeklyMoodInsights
import kotlin.math.round

class GetWeeklyMoodInsightsUseCase {

    operator fun invoke(
        entries: List<MoodEntry>,
        completedChallengesThisWeek: Int = 0,
        mindfulBreathingMinutes: Int = 0
    ): WeeklyMoodInsights {
        if (entries.isEmpty()) {
            return WeeklyMoodInsights(
                totalCheckIns = 0,
                averageMoodScore = 0.0,
                averageEnergyLevel = 0.0,
                dominantEmoji = "🌱",
                dominantMoodLabel = "Fresh Beginning",
                dailyMoodPoints = generateEmptyDailyPoints(),
                compassionateInsight = "Start logging your daily reflections to discover your personal rhythm. Every feeling is welcome here.",
                completedChallengesThisWeek = completedChallengesThisWeek,
                mindfulBreathingMinutes = mindfulBreathingMinutes
            )
        }

        val avgMood = round(entries.map { it.moodScore }.average() * 10) / 10.0
        val avgEnergy = round(entries.map { it.energyLevel }.average() * 10) / 10.0

        val dominantEmoji = entries
            .groupingBy { it.emoji }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: "🌿"

        val dominantLabel = when (dominantEmoji) {
            "😄" -> "Joyful & Uplifted"
            "😊" -> "Content & Centered"
            "🌿" -> "Peaceful & Steady"
            "🥱" -> "Resting & Recharging"
            "🌧️" -> "Tender & Reflective"
            else -> "Mindful & Aware"
        }

        val insight = when {
            avgMood >= 4.0 -> "Your emotional climate has been vibrant and uplifting. Celebrating light moments nurtures deep personal roots 🌱"
            avgMood >= 2.8 -> "A balanced, steady emotional rhythm. Staying present with the ordinary moments builds lasting resilience 🌿"
            else -> "Things felt a bit challenging or low in energy this week. Remember that resting and acknowledging tender days is an active part of growth 🌧️"
        }

        val dailyPoints = computeLast7DaysPoints(entries)

        return WeeklyMoodInsights(
            totalCheckIns = entries.size,
            averageMoodScore = avgMood,
            averageEnergyLevel = avgEnergy,
            dominantEmoji = dominantEmoji,
            dominantMoodLabel = dominantLabel,
            dailyMoodPoints = dailyPoints,
            compassionateInsight = insight,
            completedChallengesThisWeek = completedChallengesThisWeek,
            mindfulBreathingMinutes = mindfulBreathingMinutes
        )
    }

    private fun generateEmptyDailyPoints(): List<DailyMoodPoint> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return days.map { DailyMoodPoint(dayLabel = it, moodScore = null, emoji = null) }
    }

    private fun computeLast7DaysPoints(entries: List<MoodEntry>): List<DailyMoodPoint> {
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timeZone).date

        val days = (6 downTo 0).map { offset ->
            today.minus(offset, DateTimeUnit.DAY)
        }

        val entriesByDate = entries.groupBy { entry ->
            Instant.fromEpochMilliseconds(entry.timestampEpochMs)
                .toLocalDateTime(timeZone)
                .date
        }

        return days.map { date ->
            val dayName = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            val dayEntries = entriesByDate[date]
            val latest = dayEntries?.maxByOrNull { it.timestampEpochMs }

            DailyMoodPoint(
                dayLabel = dayName,
                moodScore = latest?.moodScore,
                emoji = latest?.emoji
            )
        }
    }
}
