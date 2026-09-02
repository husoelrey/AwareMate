package org.awaremate.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.awaremate.shared.domain.model.AppUsageInfo
import org.awaremate.shared.domain.model.DailyUsageSummary

@Entity(tableName = "screen_time_snapshots")
data class ScreenTimeSnapshotEntity(
    @PrimaryKey
    val dateString: String,
    val totalScreenTimeMs: Long,
    val pickupsCount: Int = 0,
    val appUsagesJson: String = "[]",
    val hourlyDistributionJson: String = "{}",
    val updatedAtEpochMs: Long = 0L
) {
    fun toDomain(): DailyUsageSummary {
        val apps = runCatching {
            Json.decodeFromString<List<AppUsageInfo>>(appUsagesJson)
        }.getOrDefault(emptyList())

        val hourly = runCatching {
            Json.decodeFromString<Map<Int, Int>>(hourlyDistributionJson)
        }.getOrDefault(emptyMap())

        return DailyUsageSummary(
            dateString = dateString,
            totalScreenTimeMs = totalScreenTimeMs,
            appUsages = apps,
            hourlyDistributionMinutes = hourly,
            pickupsCount = pickupsCount
        )
    }

    companion object {
        fun fromDomain(summary: DailyUsageSummary, timestamp: Long = 0L): ScreenTimeSnapshotEntity {
            val appsJson = runCatching {
                Json.encodeToString(summary.appUsages)
            }.getOrDefault("[]")

            val hourlyJson = runCatching {
                Json.encodeToString(summary.hourlyDistributionMinutes)
            }.getOrDefault("{}")

            return ScreenTimeSnapshotEntity(
                dateString = summary.dateString,
                totalScreenTimeMs = summary.totalScreenTimeMs,
                pickupsCount = summary.pickupsCount,
                appUsagesJson = appsJson,
                hourlyDistributionJson = hourlyJson,
                updatedAtEpochMs = timestamp
            )
        }
    }
}
