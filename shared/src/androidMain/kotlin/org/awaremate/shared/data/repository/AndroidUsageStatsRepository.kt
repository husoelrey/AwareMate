package org.awaremate.shared.data.repository

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import org.awaremate.shared.data.local.dao.ScreenTimeDao
import org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity
import org.awaremate.shared.domain.model.AppCategory
import org.awaremate.shared.domain.model.AppUsageInfo
import org.awaremate.shared.domain.model.DailyUsageSummary
import org.awaremate.shared.hasUsageStatsPermission
import org.awaremate.shared.openUsageAccessSettings

class AndroidUsageStatsRepository(
    private val context: Context,
    private val screenTimeDao: ScreenTimeDao
) : UsageStatsRepositoryImpl(screenTimeDao) {

    override fun hasPermission(): Boolean {
        return hasUsageStatsPermission(context)
    }

    override fun openPermissionSettings() {
        openUsageAccessSettings(context)
    }

    override suspend fun getDailyUsage(
        dateString: String,
        startOfDayEpochMs: Long,
        endOfDayEpochMs: Long
    ): DailyUsageSummary {
        if (!hasPermission()) {
            val cached = screenTimeDao.getSnapshot(dateString)
            if (cached != null) return cached.toDomain()
            val defaultUsage = createDefaultUsage(dateString)
            screenTimeDao.insertSnapshot(ScreenTimeSnapshotEntity.fromDomain(defaultUsage, endOfDayEpochMs))
            return defaultUsage
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return super.getDailyUsage(dateString, startOfDayEpochMs, endOfDayEpochMs)

        val stats: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDayEpochMs,
            endOfDayEpochMs
        ) ?: emptyList()

        val pm = context.packageManager

        val appUsages = stats
            .filter { it.totalTimeInForeground > 60_000L }
            .map { usage ->
                val appName = runCatching {
                    val appInfo = pm.getApplicationInfo(usage.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                }.getOrDefault(usage.packageName)

                val category = categorizeApp(usage.packageName, pm)

                AppUsageInfo(
                    packageName = usage.packageName,
                    appName = appName,
                    totalTimeInForegroundMs = usage.totalTimeInForeground,
                    lastTimeUsedEpochMs = usage.lastTimeUsed,
                    category = category
                )
            }
            .sortedByDescending { it.totalTimeInForegroundMs }

        val totalScreenTimeMs = appUsages.sumOf { it.totalTimeInForegroundMs }
        val hourlyDistribution = estimateHourlyDistribution(totalScreenTimeMs)

        val summary = DailyUsageSummary(
            dateString = dateString,
            totalScreenTimeMs = totalScreenTimeMs,
            appUsages = appUsages,
            hourlyDistributionMinutes = hourlyDistribution,
            pickupsCount = (appUsages.size * 3).coerceAtLeast(12)
        )

        screenTimeDao.insertSnapshot(ScreenTimeSnapshotEntity.fromDomain(summary, endOfDayEpochMs))
        return summary
    }

    private fun categorizeApp(packageName: String, pm: PackageManager): AppCategory {
        val lower = packageName.lowercase()
        return when {
            lower.contains("chrome") || lower.contains("browser") || lower.contains("firefox") -> AppCategory.PRODUCTIVITY
            lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("instagram") ||
                    lower.contains("facebook") || lower.contains("twitter") || lower.contains("tiktok") ||
                    lower.contains("discord") || lower.contains("social") -> AppCategory.SOCIAL_COMMUNICATION
            lower.contains("youtube") || lower.contains("netflix") || lower.contains("spotify") ||
                    lower.contains("game") || lower.contains("twitch") -> AppCategory.ENTERTAINMENT_GAMES
            lower.contains("book") || lower.contains("duolingo") || lower.contains("learn") ||
                    lower.contains("study") || lower.contains("wiki") -> AppCategory.EDUCATION
            lower.contains("fit") || lower.contains("health") || lower.contains("meditat") ||
                    lower.contains("awaremate") -> AppCategory.HEALTH_WELLNESS
            else -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    runCatching {
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        when (appInfo.category) {
                            ApplicationInfo.CATEGORY_GAME -> AppCategory.ENTERTAINMENT_GAMES
                            ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_IMAGE -> AppCategory.ENTERTAINMENT_GAMES
                            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL_COMMUNICATION
                            ApplicationInfo.CATEGORY_NEWS -> AppCategory.PRODUCTIVITY
                            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
                            else -> AppCategory.OTHER
                        }
                    }.getOrDefault(AppCategory.OTHER)
                } else {
                    AppCategory.OTHER
                }
            }
        }
    }

    private fun estimateHourlyDistribution(totalMs: Long): Map<Int, Int> {
        val totalMinutes = (totalMs / (1000 * 60)).toInt()
        if (totalMinutes <= 0) return emptyMap()

        val weights = mapOf(
            8 to 0.05, 9 to 0.08, 10 to 0.09, 11 to 0.07, 12 to 0.12,
            13 to 0.08, 14 to 0.06, 15 to 0.08, 16 to 0.07, 17 to 0.09,
            18 to 0.08, 19 to 0.05, 20 to 0.04, 21 to 0.04
        )
        return weights.mapValues { (_, weight) -> (totalMinutes * weight).toInt() }
    }
}
