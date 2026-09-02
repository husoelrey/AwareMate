package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.DailyUsageSummary

interface UsageStatsRepository {
    fun hasPermission(): Boolean
    fun openPermissionSettings()
    suspend fun getDailyUsage(dateString: String, startOfDayEpochMs: Long, endOfDayEpochMs: Long): DailyUsageSummary
    suspend fun getWeeklyUsage(days: List<Pair<String, LongRange>>): List<DailyUsageSummary>
    fun observeDailyUsage(dateString: String): Flow<DailyUsageSummary?>
}
