package org.awaremate.shared.data.repository

import kotlinx.coroutines.test.runTest
import org.awaremate.shared.domain.model.AppCategory
import org.awaremate.shared.domain.model.AppUsageInfo
import org.awaremate.shared.domain.model.DailyUsageSummary
import org.awaremate.shared.test.FakeScreenTimeDao
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UsageStatsRepositoryTest {

    private lateinit var fakeDao: FakeScreenTimeDao
    private lateinit var repository: UsageStatsRepositoryImpl

    @BeforeTest
    fun setUp() {
        fakeDao = FakeScreenTimeDao()
        repository = UsageStatsRepositoryImpl(screenTimeDao = fakeDao)
    }

    @Test
    fun testGetDailyUsageCachesToDatabase() = runTest {
        val dateString = "2026-09-02"
        val summary = repository.getDailyUsage(dateString, 1000L, 2000L)

        assertNotNull(summary)
        assertEquals(dateString, summary.dateString)
        assertTrue(summary.totalScreenTimeMs > 0)
        assertTrue(summary.appUsages.isNotEmpty())

        // Verify it was stored in Room DAO
        val cached = fakeDao.getSnapshot(dateString)
        assertNotNull(cached)
        assertEquals(dateString, cached.dateString)
        assertEquals(summary.totalScreenTimeMs, cached.totalScreenTimeMs)
    }

    @Test
    fun testGetWeeklyUsageRetrievesAllDays() = runTest {
        val days = listOf(
            "2026-08-27" to (100L..200L),
            "2026-08-28" to (201L..300L),
            "2026-08-29" to (301L..400L),
            "2026-08-30" to (401L..500L),
            "2026-08-31" to (501L..600L),
            "2026-09-01" to (601L..700L),
            "2026-09-02" to (701L..800L)
        )

        val weekly = repository.getWeeklyUsage(days)
        assertEquals(7, weekly.size)
        assertEquals("2026-08-27", weekly.first().dateString)
        assertEquals("2026-09-02", weekly.last().dateString)
    }

    @Test
    fun testAppUsageFormatting() {
        val app = AppUsageInfo(
            packageName = "org.example",
            appName = "Example App",
            totalTimeInForegroundMs = 75 * 60 * 1000L, // 75 mins = 1h 15m
            category = AppCategory.PRODUCTIVITY
        )

        assertEquals(75, app.totalMinutes)
        assertEquals("1h 15m", app.formattedTime)

        val shortApp = AppUsageInfo(
            packageName = "org.short",
            appName = "Short App",
            totalTimeInForegroundMs = 25 * 60 * 1000L, // 25 mins
            category = AppCategory.EDUCATION
        )
        assertEquals(25, shortApp.totalMinutes)
        assertEquals("25m", shortApp.formattedTime)
    }
}
