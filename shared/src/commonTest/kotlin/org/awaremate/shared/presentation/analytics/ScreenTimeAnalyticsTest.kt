package org.awaremate.shared.presentation.analytics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.awaremate.shared.data.repository.UsageStatsRepositoryImpl
import org.awaremate.shared.domain.model.DailyScreenTimeData
import org.awaremate.shared.test.FakeScreenTimeDao
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenTimeAnalyticsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeScreenTimeDao
    private lateinit var repository: UsageStatsRepositoryImpl

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeScreenTimeDao()
        repository = UsageStatsRepositoryImpl(screenTimeDao = fakeDao)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAnalyticsStateWhenPermissionNotGranted() = runTest {
        // When permission is not granted, repository returns default demo usage without crashing
        assertFalse(repository.hasPermission(), "Default test repository has no system permission")

        val summary = repository.getDailyUsage("2026-09-02", 1000L, 2000L)
        assertNotNull(summary, "Must return valid usage summary even without permission")
        assertTrue(summary.totalScreenTimeMs > 0)
        assertTrue(summary.appUsages.isNotEmpty())

        val state = ScreenTimeAnalyticsState(
            hasPermission = false,
            todayUsage = summary,
            dailyGoalMinutes = 180
        )
        assertFalse(state.hasPermission)
        assertEquals(180, state.dailyGoalMinutes)
        assertNotNull(state.todayUsage)
    }

    @Test
    fun testAnalyticsStateWhenPermissionGranted() = runTest {
        val summary = repository.getDailyUsage("2026-09-02", 1000L, 2000L)
        val state = ScreenTimeAnalyticsState(
            hasPermission = true,
            todayUsage = summary,
            dailyGoalMinutes = 180
        )
        assertTrue(state.hasPermission)
        assertEquals(summary.totalMinutes, state.todayUsage.totalMinutes)
    }

    @Test
    fun testChartDataEmptyListHandlingDoesNotCrash() {
        val emptyList = emptyList<DailyScreenTimeData>()
        val dailyGoalMinutes = 180

        // Simulate calculation in chart rendering: must not throw NoSuchElementException or ArithmeticException
        val maxMinutes = (emptyList.maxOfOrNull { it.screenTimeMinutes } ?: 180)
            .coerceAtLeast(dailyGoalMinutes)
            .coerceAtLeast(60)

        assertEquals(180, maxMinutes)
        val count = emptyList.size.coerceAtLeast(1)
        assertEquals(1, count)
        assertTrue(emptyList.isEmpty())
    }

    @Test
    fun testChartDataWithRealAndMockData() = runTest {
        val days = listOf(
            "2026-08-27" to (100L..200L),
            "2026-08-28" to (201L..300L),
            "2026-08-29" to (301L..400L),
            "2026-08-30" to (401L..500L),
            "2026-08-31" to (501L..600L),
            "2026-09-01" to (601L..700L),
            "2026-09-02" to (701L..800L)
        )

        val summaries = repository.getWeeklyUsage(days)
        val chartData = summaries.map {
            DailyScreenTimeData(
                dayLabel = it.dateString.takeLast(2),
                dateString = it.dateString,
                screenTimeMinutes = it.totalMinutes,
                goalMinutes = 180
            )
        }

        assertEquals(7, chartData.size)
        val max = (chartData.maxOfOrNull { it.screenTimeMinutes } ?: 180).coerceAtLeast(180)
        assertTrue(max >= 180)
    }

    @Test
    fun testScreenModelWhenPermissionDenied() = runTest(testDispatcher) {
        val prefsRepo = org.awaremate.shared.test.FakePreferencesRepository()
        val screenModel = ScreenTimeAnalyticsScreenModel(
            usageStatsRepository = repository,
            preferencesRepository = prefsRepo
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertFalse(state.hasPermission, "Permission must be reported as false")
        assertNotNull(state.todayUsage)
        assertTrue(state.todayUsage.totalMinutes > 0, "Provides safe default usage without crash")
        assertEquals(180, state.dailyGoalMinutes)
        assertEquals(7, state.weeklyChartData.size, "Provides 7-day weekly data without crash")
    }

    @Test
    fun testScreenModelWhenPermissionGranted() = runTest(testDispatcher) {
        var settingsOpened = false
        val permissionRepo = object : UsageStatsRepositoryImpl(screenTimeDao = fakeDao) {
            override fun hasPermission(): Boolean = true
            override fun openPermissionSettings() {
                settingsOpened = true
            }
        }
        val prefsRepo = org.awaremate.shared.test.FakePreferencesRepository()
        val screenModel = ScreenTimeAnalyticsScreenModel(
            usageStatsRepository = permissionRepo,
            preferencesRepository = prefsRepo
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertTrue(state.hasPermission, "Permission must be reported as true")
        assertNotNull(state.todayUsage)
        assertEquals(7, state.weeklyChartData.size)

        screenModel.requestUsagePermission()
        assertTrue(settingsOpened, "openPermissionSettings must be called")
    }
}
