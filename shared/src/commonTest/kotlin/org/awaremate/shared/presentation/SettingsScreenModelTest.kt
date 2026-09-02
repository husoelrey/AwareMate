package org.awaremate.shared.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.awaremate.shared.presentation.settings.SettingsIntent
import org.awaremate.shared.presentation.settings.SettingsScreenModel
import org.awaremate.shared.test.FakePreferencesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var screenModel: SettingsScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePreferencesRepository = FakePreferencesRepository()
        screenModel = SettingsScreenModel(fakePreferencesRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialSettings() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        val prefs = screenModel.state.value.preferences
        assertEquals("SYSTEM", prefs.themeMode)
        assertTrue(prefs.dynamicColorEnabled)
        assertTrue(prefs.notificationsEnabled)
        assertEquals(180, prefs.dailyScreenTimeGoalMinutes)
        assertEquals(30, prefs.nudgeThresholdMinutes)
    }

    @Test
    fun testChangeThemeMode() = runTest(testDispatcher) {
        screenModel.handleIntent(SettingsIntent.SetThemeMode("DARK"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("DARK", screenModel.state.value.preferences.themeMode)

        screenModel.handleIntent(SettingsIntent.SetThemeMode("LIGHT"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("LIGHT", screenModel.state.value.preferences.themeMode)
    }

    @Test
    fun testChangeDynamicColor() = runTest(testDispatcher) {
        screenModel.handleIntent(SettingsIntent.SetDynamicColor(false))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(screenModel.state.value.preferences.dynamicColorEnabled)
    }

    @Test
    fun testChangeNotifications() = runTest(testDispatcher) {
        screenModel.handleIntent(SettingsIntent.SetNotifications(false))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(screenModel.state.value.preferences.notificationsEnabled)
    }

    @Test
    fun testChangeGoalsAndBedtime() = runTest(testDispatcher) {
        screenModel.handleIntent(SettingsIntent.SetDailyGoal(240))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(240, screenModel.state.value.preferences.dailyScreenTimeGoalMinutes)

        screenModel.handleIntent(SettingsIntent.SetNudgeThreshold(45))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(45, screenModel.state.value.preferences.nudgeThresholdMinutes)

        screenModel.handleIntent(SettingsIntent.SetBedtime(23, 15))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(23, screenModel.state.value.preferences.bedtimeHour)
        assertEquals(15, screenModel.state.value.preferences.bedtimeMinute)
    }
}
