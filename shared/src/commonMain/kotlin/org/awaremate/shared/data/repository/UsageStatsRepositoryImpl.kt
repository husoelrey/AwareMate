package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.awaremate.shared.data.local.dao.ScreenTimeDao
import org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity
import org.awaremate.shared.domain.model.AppCategory
import org.awaremate.shared.domain.model.AppUsageInfo
import org.awaremate.shared.domain.model.DailyUsageSummary
import org.awaremate.shared.domain.repository.UsageStatsRepository
import org.awaremate.shared.hasUsageStatsPermission
import org.awaremate.shared.openUsageAccessSettings

open class UsageStatsRepositoryImpl(
    private val screenTimeDao: ScreenTimeDao
) : UsageStatsRepository {

    override fun hasPermission(): Boolean {
        return hasUsageStatsPermission(null)
    }

    override fun openPermissionSettings() {
        openUsageAccessSettings(null)
    }

    override suspend fun getDailyUsage(
        dateString: String,
        startOfDayEpochMs: Long,
        endOfDayEpochMs: Long
    ): DailyUsageSummary {
        // Check Room cache first
        val cached = screenTimeDao.getSnapshot(dateString)
        if (cached != null) {
            return cached.toDomain()
        }

        // Generate demo/placeholder usage if no platform provider or permission
        val placeholder = createDefaultUsage(dateString)
        screenTimeDao.insertSnapshot(ScreenTimeSnapshotEntity.fromDomain(placeholder, endOfDayEpochMs))
        return placeholder
    }

    override suspend fun getWeeklyUsage(days: List<Pair<String, LongRange>>): List<DailyUsageSummary> {
        return days.map { (dateString, range) ->
            getDailyUsage(dateString, range.first, range.last)
        }
    }

    override fun observeDailyUsage(dateString: String): Flow<DailyUsageSummary?> {
        return screenTimeDao.getSnapshotFlow(dateString).map { it?.toDomain() }
    }

    protected fun createDefaultUsage(dateString: String): DailyUsageSummary {
        val demoApps = listOf(
            AppUsageInfo(
                packageName = "org.awaremate.android",
                appName = "AwareMate",
                totalTimeInForegroundMs = 25 * 60 * 1000L,
                category = AppCategory.HEALTH_WELLNESS
            ),
            AppUsageInfo(
                packageName = "com.example.reading",
                appName = "E-Book Reader",
                totalTimeInForegroundMs = 45 * 60 * 1000L,
                category = AppCategory.EDUCATION
            ),
            AppUsageInfo(
                packageName = "com.example.messaging",
                appName = "Messages",
                totalTimeInForegroundMs = 30 * 60 * 1000L,
                category = AppCategory.SOCIAL_COMMUNICATION
            )
        )
        val totalMs = demoApps.sumOf { it.totalTimeInForegroundMs }
        val hourly = mapOf(
            9 to 15,
            12 to 20,
            15 to 30,
            18 to 25,
            20 to 10
        )
        return DailyUsageSummary(
            dateString = dateString,
            totalScreenTimeMs = totalMs,
            appUsages = demoApps,
            hourlyDistributionMinutes = hourly,
            pickupsCount = 28
        )
    }
}
