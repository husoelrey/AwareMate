package org.awaremate.shared.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.awaremate.shared.presentation.settings.SettingsIntent
import org.awaremate.shared.presentation.settings.SettingsScreenModel
import org.awaremate.shared.data.remote.ConnectivityObserver
import org.awaremate.shared.domain.model.UserPreferences
import org.awaremate.shared.domain.service.MissedCheckInReminderScheduler
import org.awaremate.shared.domain.usecase.account.DeleteAccountUseCase
import org.awaremate.shared.test.FakeAccountDataDao
import org.awaremate.shared.test.FakeAccountDeletionService
import org.awaremate.shared.test.FakeAuthService
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
        assertTrue(prefs.missedCheckInReminderEnabled)
        assertEquals(18, prefs.missedCheckInReminderHour)
    }

    @Test
    fun testChangeMissedCheckInReminderSettings() = runTest(testDispatcher) {
        screenModel.handleIntent(SettingsIntent.SetMissedCheckInReminder(false))
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(screenModel.state.value.preferences.missedCheckInReminderEnabled)

        screenModel.handleIntent(SettingsIntent.SetMissedCheckInTime(20))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(20, screenModel.state.value.preferences.missedCheckInReminderHour)
    }

    @Test
    fun disablingMissedCheckInReminderRefreshesSchedulerAndStaysDisabled() = runTest(testDispatcher) {
        val scheduler = RecordingReminderScheduler()
        val model = SettingsScreenModel(fakePreferencesRepository, scheduler)
        testDispatcher.scheduler.advanceUntilIdle()

        model.handleIntent(SettingsIntent.SetMissedCheckInReminder(false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, scheduler.refreshCalls)
        assertFalse(fakePreferencesRepository.getPreferences().first().missedCheckInReminderEnabled)

        val recreatedModel = SettingsScreenModel(fakePreferencesRepository, scheduler)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(recreatedModel.state.value.preferences.missedCheckInReminderEnabled)
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

    @Test
    fun offlineDeleteIntentShowsSafeErrorAndPreservesEverything() = runTest(testDispatcher) {
        val auth = FakeAuthService().also { it.signInAnonymously().getOrThrow() }
        val remote = FakeAccountDeletionService()
        val local = FakeAccountDataDao()
        val preferences = FakePreferencesRepository(UserPreferences(onboardingCompleted = true))
        val connectivity = object : ConnectivityObserver {
            override val isOnline = MutableStateFlow(false)
        }
        val model = SettingsScreenModel(
            preferencesRepository = preferences,
            deleteAccountUseCase = DeleteAccountUseCase(connectivity, auth, remote, local, preferences)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        model.handleIntent(SettingsIntent.DeleteAccount)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "You're offline. Nothing was deleted. Reconnect and try again when you're ready.",
            model.state.value.accountDeletionError
        )
        assertEquals(null, remote.deletedUserId)
        assertEquals(0, local.clearCalls)
        assertEquals(0, auth.signOutCalls)
        assertEquals("anon-123", auth.currentUser?.id)
        assertTrue(preferences.getPreferences().first().onboardingCompleted)
    }
}

private class RecordingReminderScheduler : MissedCheckInReminderScheduler {
    var refreshCalls = 0

    override suspend fun refresh() {
        refreshCalls += 1
    }
}
