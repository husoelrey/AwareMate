package org.awaremate.shared.presentation.analytics

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.awaremate.shared.domain.model.DailyScreenTimeData
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.domain.repository.UsageStatsRepository

class ScreenTimeAnalyticsScreenModel(
    private val usageStatsRepository: UsageStatsRepository,
    private val preferencesRepository: PreferencesRepository? = null
) : ScreenModel {

    private val _state = MutableStateFlow(ScreenTimeAnalyticsState())
    val state: StateFlow<ScreenTimeAnalyticsState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        screenModelScope.launch {
            val hasPerm = usageStatsRepository.hasPermission()
            val prefs = preferencesRepository?.getPreferences()?.firstOrNull()
            val goal = prefs?.dailyScreenTimeGoalMinutes ?: 180

            val nowMs = Clock.System.now().toEpochMilliseconds()
            val timeZone = TimeZone.currentSystemDefault()
            val todayStr = Clock.System.todayIn(timeZone).toString()
            val startOfDayMs = nowMs - (nowMs % (24 * 60 * 60 * 1000L))

            val todayUsage = usageStatsRepository.getDailyUsage(todayStr, startOfDayMs, nowMs)

            // Calculate 7-day breakdown for weekly chart
            val pastDays = (6 downTo 0).map { offset ->
                val dayEpoch = nowMs - (offset * 24 * 60 * 60 * 1000L)
                val dayStart = dayEpoch - (dayEpoch % (24 * 60 * 60 * 1000L))
                val dayEnd = dayStart + (24 * 60 * 60 * 1000L) - 1
                val dayDateStr = Instant.fromEpochMilliseconds(dayStart)
                    .toLocalDateTime(timeZone).date.toString()
                dayDateStr to (dayStart..dayEnd)
            }

            val weeklySummaries = usageStatsRepository.getWeeklyUsage(pastDays)
            val chartData = weeklySummaries.map { summary ->
                DailyScreenTimeData(
                    dayLabel = summary.dateString.takeLast(2),
                    dateString = summary.dateString,
                    screenTimeMinutes = summary.totalMinutes,
                    goalMinutes = goal
                )
            }

            _state.update {
                it.copy(
                    hasPermission = hasPerm,
                    todayUsage = todayUsage,
                    dailyGoalMinutes = goal,
                    weeklyChartData = chartData
                )
            }
        }
    }

    fun requestUsagePermission() {
        usageStatsRepository.openPermissionSettings()
    }
}
