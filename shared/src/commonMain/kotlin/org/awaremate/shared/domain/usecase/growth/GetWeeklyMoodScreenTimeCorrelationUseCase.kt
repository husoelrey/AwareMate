package org.awaremate.shared.domain.usecase.growth

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.awaremate.shared.domain.model.DailyUsageSummary
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimeCorrelation
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimePoint

class GetWeeklyMoodScreenTimeCorrelationUseCase {
    companion object {
        const val MINIMUM_MOOD_DAYS = 5
    }

    fun currentWeekRanges(
        nowEpochMs: Long = Clock.System.now().toEpochMilliseconds(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): List<Pair<String, LongRange>> {
        val today = Instant.fromEpochMilliseconds(nowEpochMs).toLocalDateTime(timeZone).date
        val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        return (0..today.dayOfWeek.ordinal).map { offset ->
            val date = monday.plus(offset, DateTimeUnit.DAY)
            val start = date.atStartOfDayIn(timeZone).toEpochMilliseconds()
            val end = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds() - 1
            date.toString() to (start..end)
        }
    }

    fun hasEnoughMoodDays(
        entries: List<MoodEntry>,
        nowEpochMs: Long = Clock.System.now().toEpochMilliseconds(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Boolean {
        val dates = currentWeekDates(nowEpochMs, timeZone)
        return entries
            .asSequence()
            .map { Instant.fromEpochMilliseconds(it.timestampEpochMs).toLocalDateTime(timeZone).date }
            .filter { it in dates }
            .distinct()
            .count() >= MINIMUM_MOOD_DAYS
    }

    operator fun invoke(
        entries: List<MoodEntry>,
        usageSummaries: List<DailyUsageSummary>,
        nowEpochMs: Long = Clock.System.now().toEpochMilliseconds(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): WeeklyMoodScreenTimeCorrelation {
        if (!hasEnoughMoodDays(entries, nowEpochMs, timeZone)) {
            return WeeklyMoodScreenTimeCorrelation(hasEnoughMoodDays = false)
        }

        val weekDates = currentWeekDates(nowEpochMs, timeZone)
        val latestMoodByDate = entries
            .groupBy { Instant.fromEpochMilliseconds(it.timestampEpochMs).toLocalDateTime(timeZone).date }
            .mapValues { (_, values) -> values.maxBy { it.timestampEpochMs } }
        val usageByDate = usageSummaries.associateBy { it.dateString }
        val points = weekDates.mapNotNull { date ->
            val mood = latestMoodByDate[date] ?: return@mapNotNull null
            val usage = usageByDate[date.toString()] ?: return@mapNotNull null
            WeeklyMoodScreenTimePoint(
                dayLabel = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                dateString = date.toString(),
                moodScore = mood.moodScore,
                energyLevel = mood.energyLevel,
                screenTimeMinutes = usage.totalMinutes
            )
        }

        return WeeklyMoodScreenTimeCorrelation(
            hasEnoughMoodDays = true,
            points = points,
            observationalInsight = observationalInsight(points)
        )
    }

    private fun currentWeekDates(nowEpochMs: Long, timeZone: TimeZone): List<LocalDate> {
        val today = Instant.fromEpochMilliseconds(nowEpochMs).toLocalDateTime(timeZone).date
        val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        return (0..today.dayOfWeek.ordinal).map { monday.plus(it, DateTimeUnit.DAY) }
    }

    private fun observationalInsight(points: List<WeeklyMoodScreenTimePoint>): String? {
        if (points.size < MINIMUM_MOOD_DAYS) return null
        val ordered = points.sortedBy { it.screenTimeMinutes }
        val splitIndex = ordered.size / 2
        val lowerScreenTimeEnergy = ordered.take(splitIndex).map { it.energyLevel }.average()
        val higherScreenTimeEnergy = ordered.takeLast(splitIndex).map { it.energyLevel }.average()
        return when {
            lowerScreenTimeEnergy >= higherScreenTimeEnergy + 0.5 ->
                "On lower screen-time days this week, your energy tended to be higher."
            higherScreenTimeEnergy >= lowerScreenTimeEnergy + 0.5 ->
                "On higher screen-time days this week, your energy tended to be higher."
            else -> "Your energy and screen-time patterns varied across the week."
        }
    }
}
