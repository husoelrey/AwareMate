package org.awaremate.shared.domain.usecase.awareness

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.awaremate.shared.domain.model.AppUsageInfo
import org.awaremate.shared.domain.model.DailyScreenTimeData
import org.awaremate.shared.domain.model.DailyUsageSummary
import org.awaremate.shared.domain.model.WeeklyDigitalAwarenessReport
import org.awaremate.shared.domain.repository.CompanionRepository
import org.awaremate.shared.domain.repository.FocusSessionRepository
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.domain.repository.UsageStatsRepository

class GetWeeklyAwarenessReportUseCase(
    private val usageStatsRepository: UsageStatsRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val companionRepository: CompanionRepository,
    private val preferencesRepository: PreferencesRepository,
    private val calculateAwarenessScoreUseCase: CalculateAwarenessScoreUseCase = CalculateAwarenessScoreUseCase()
) {

    suspend operator fun invoke(
        referenceEpochMs: Long = Clock.System.now().toEpochMilliseconds()
    ): WeeklyDigitalAwarenessReport {
        val prefs = preferencesRepository.getPreferences().first()
        val dailyGoalMinutes = prefs.dailyScreenTimeGoalMinutes

        // Generate 7 days ranges (today and 6 previous days)
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val days = mutableListOf<Pair<String, LongRange>>()
        val dayDataList = mutableListOf<DailyScreenTimeData>()

        val oneDayMs = 24 * 60 * 60 * 1000L
        for (i in 6 downTo 0) {
            val dayEpoch = referenceEpochMs - (i * oneDayMs)
            val dt = Instant.fromEpochMilliseconds(dayEpoch).toLocalDateTime(TimeZone.currentSystemDefault())
            val dateStr = "${dt.year}-${dt.monthNumber.toString().padStart(2, '0')}-${dt.dayOfMonth.toString().padStart(2, '0')}"
            val startOfDay = dayEpoch - (dayEpoch % oneDayMs)
            val endOfDay = startOfDay + oneDayMs - 1
            days.add(dateStr to (startOfDay..endOfDay))
        }

        val dailySummaries: List<DailyUsageSummary> = usageStatsRepository.getWeeklyUsage(days)

        // Map into chart data
        dailySummaries.forEachIndexed { index, summary ->
            val label = if (index in dayLabels.indices) dayLabels[index] else "Day $index"
            dayDataList.add(
                DailyScreenTimeData(
                    dayLabel = label,
                    dateString = summary.dateString,
                    screenTimeMinutes = summary.totalMinutes,
                    goalMinutes = dailyGoalMinutes
                )
            )
        }

        val totalScreenTimeMinutes = dailySummaries.sumOf { it.totalMinutes }
        val dailyAverageMinutes = if (dailySummaries.isNotEmpty()) totalScreenTimeMinutes / dailySummaries.size else 0

        // Focus sessions
        val weekStartEpoch = days.firstOrNull()?.second?.first ?: referenceEpochMs - (7 * oneDayMs)
        val weekEndEpoch = days.lastOrNull()?.second?.last ?: referenceEpochMs
        val recentSessions = focusSessionRepository.getRecentSessions(100).first()
        val weeklySessions = recentSessions.filter { it.startTimeEpochMs in weekStartEpoch..weekEndEpoch }
        val totalFocusMinutes = (weeklySessions.sumOf { it.durationSeconds } / 60)

        // Top Apps aggregation across week
        val appAggregator = mutableMapOf<String, AppUsageInfo>()
        dailySummaries.flatMap { it.appUsages }.forEach { app ->
            val existing = appAggregator[app.packageName]
            if (existing == null) {
                appAggregator[app.packageName] = app
            } else {
                appAggregator[app.packageName] = existing.copy(
                    totalTimeInForegroundMs = existing.totalTimeInForegroundMs + app.totalTimeInForegroundMs
                )
            }
        }
        val topApps = appAggregator.values.sortedByDescending { it.totalTimeInForegroundMs }.take(5)

        // Companion & momentum
        val companion = companionRepository.getCompanion().firstOrNull()
        val momentum = companion?.momentumScore ?: 50.0
        val momentumTrend = when {
            momentum >= 80.0 -> "Thriving Momentum 🌟"
            momentum >= 50.0 -> "Steady Steady Growth 🌱"
            else -> "Gentle Beginning 🌿"
        }

        // Composite awareness score average
        val awarenessScore = calculateAwarenessScoreUseCase(
            screenTimeMinutes = dailyAverageMinutes,
            targetScreenTimeGoalMinutes = dailyGoalMinutes,
            focusSessionMinutes = (totalFocusMinutes / 7).coerceAtLeast(0),
            moodEntriesCount = 5,
            completedChallengesCount = 4
        ).totalScore

        // Synthesize non-punitive insight
        val compassionateInsight = generateCompassionateInsight(
            totalFocusMinutes = totalFocusMinutes,
            dailyAverageMinutes = dailyAverageMinutes,
            dailyGoalMinutes = dailyGoalMinutes
        )

        return WeeklyDigitalAwarenessReport(
            startDate = days.firstOrNull()?.first ?: "",
            endDate = days.lastOrNull()?.first ?: "",
            dailySummaries = dailySummaries,
            totalScreenTimeMinutes = totalScreenTimeMinutes,
            dailyAverageMinutes = dailyAverageMinutes,
            totalFocusMinutes = totalFocusMinutes,
            focusSessionsCount = weeklySessions.size,
            averageAwarenessScore = awarenessScore,
            momentumTrend = momentumTrend,
            compassionateInsight = compassionateInsight,
            topApps = topApps
        )
    }

    private fun generateCompassionateInsight(
        totalFocusMinutes: Int,
        dailyAverageMinutes: Int,
        dailyGoalMinutes: Int
    ): String {
        return when {
            totalFocusMinutes >= 120 ->
                "🌱 You dedicated $totalFocusMinutes mindful focus minutes this week! Your ability to carve out deep presence with your companion is blossoming beautifully."
            dailyAverageMinutes <= dailyGoalMinutes && dailyGoalMinutes > 0 ->
                "🌤️ Your digital habits remained in harmony with your intentions this week. Thank yourself for noticing your needs and pausing often."
            else ->
                "🌿 Every week brings a different rhythm. Even during busier screen days, your awareness stayed present. Your plant companion is proud to walk beside you."
        }
    }
}
