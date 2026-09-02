package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppCategory {
    EDUCATION,
    PRODUCTIVITY,
    SOCIAL_COMMUNICATION,
    ENTERTAINMENT_GAMES,
    CREATIVITY_TOOLS,
    HEALTH_WELLNESS,
    OTHER
}

@Serializable
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMs: Long,
    val lastTimeUsedEpochMs: Long = 0L,
    val category: AppCategory = AppCategory.OTHER
) {
    val totalMinutes: Int get() = (totalTimeInForegroundMs / (1000 * 60)).toInt()

    val formattedTime: String get() {
        val totalSec = totalTimeInForegroundMs / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

@Serializable
data class DailyUsageSummary(
    val dateString: String,
    val totalScreenTimeMs: Long,
    val appUsages: List<AppUsageInfo> = emptyList(),
    val hourlyDistributionMinutes: Map<Int, Int> = emptyMap(),
    val pickupsCount: Int = 0
) {
    val totalMinutes: Int get() = (totalScreenTimeMs / (1000 * 60)).toInt()

    val formattedTime: String get() {
        val totalSec = totalScreenTimeMs / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

@Serializable
data class DailyScreenTimeData(
    val dayLabel: String,
    val dateString: String,
    val screenTimeMinutes: Int,
    val goalMinutes: Int
)

@Serializable
data class WeeklyDigitalAwarenessReport(
    val startDate: String,
    val endDate: String,
    val dailySummaries: List<DailyUsageSummary>,
    val totalScreenTimeMinutes: Int,
    val dailyAverageMinutes: Int,
    val totalFocusMinutes: Int,
    val focusSessionsCount: Int,
    val averageAwarenessScore: Int,
    val momentumTrend: String,
    val compassionateInsight: String,
    val topApps: List<AppUsageInfo> = emptyList()
)
