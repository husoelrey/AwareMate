package org.awaremate.shared.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.domain.usecase.companion.SaveCompanionUseCase
import org.awaremate.shared.presentation.onboarding.OnboardingIntent
import org.awaremate.shared.presentation.onboarding.OnboardingScreenModel
import org.awaremate.shared.presentation.onboarding.OnboardingStep
import org.awaremate.shared.presentation.onboarding.UserInterest
import org.awaremate.shared.test.FakeCompanionDao
import org.awaremate.shared.test.FakePreferencesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var fakeCompanionDao: FakeCompanionDao
    private lateinit var saveCompanionUseCase: SaveCompanionUseCase
    private lateinit var screenModel: OnboardingScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePreferencesRepository = FakePreferencesRepository()
        fakeCompanionDao = FakeCompanionDao()
        val companionRepository = CompanionRepositoryImpl(companionDao = fakeCompanionDao)
        saveCompanionUseCase = SaveCompanionUseCase(companionRepository)
        screenModel = OnboardingScreenModel(fakePreferencesRepository, saveCompanionUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val state = screenModel.state.value
        assertEquals(OnboardingStep.WELCOME, state.currentStep)
        assertEquals("Sprout", state.companionName)
        assertTrue(state.notificationsEnabled)
        assertEquals(180, state.dailyScreenTimeGoalMinutes)
        assertFalse(state.isCompleted)
    }

    @Test
    fun testStepNavigationForwardAndBackward() {
        // Forward: WELCOME -> INTERESTS -> COMPANION_NAMING -> PERMISSIONS -> INTENTIONS
        screenModel.handleIntent(OnboardingIntent.NextStep)
        assertEquals(OnboardingStep.INTERESTS, screenModel.state.value.currentStep)

        screenModel.handleIntent(OnboardingIntent.NextStep)
        assertEquals(OnboardingStep.COMPANION_NAMING, screenModel.state.value.currentStep)

        screenModel.handleIntent(OnboardingIntent.NextStep)
        assertEquals(OnboardingStep.PERMISSIONS, screenModel.state.value.currentStep)

        screenModel.handleIntent(OnboardingIntent.NextStep)
        assertEquals(OnboardingStep.INTENTIONS, screenModel.state.value.currentStep)

        // Backward: INTENTIONS -> PERMISSIONS
        screenModel.handleIntent(OnboardingIntent.PreviousStep)
        assertEquals(OnboardingStep.PERMISSIONS, screenModel.state.value.currentStep)
    }

    @Test
    fun testToggleInterests() {
        screenModel.handleIntent(OnboardingIntent.ToggleInterest(UserInterest.CREATIVE_PURSUITS))
        assertTrue(screenModel.state.value.selectedInterests.contains(UserInterest.CREATIVE_PURSUITS))

        screenModel.handleIntent(OnboardingIntent.ToggleInterest(UserInterest.CREATIVE_PURSUITS))
        assertFalse(screenModel.state.value.selectedInterests.contains(UserInterest.CREATIVE_PURSUITS))
    }

    @Test
    fun testSetCompanionName() {
        screenModel.handleIntent(OnboardingIntent.SetCompanionName("Fern"))
        assertEquals("Fern", screenModel.state.value.companionName)
    }

    @Test
    fun testSetIntentions() {
        screenModel.handleIntent(OnboardingIntent.SetScreenTimeGoal(240))
        assertEquals(240, screenModel.state.value.dailyScreenTimeGoalMinutes)

        screenModel.handleIntent(OnboardingIntent.SetNudgeThreshold(45))
        assertEquals(45, screenModel.state.value.nudgeThresholdMinutes)

        screenModel.handleIntent(OnboardingIntent.SetBedtime(23, 0))
        assertEquals(23, screenModel.state.value.bedtimeHour)
        assertEquals(0, screenModel.state.value.bedtimeMinute)
    }

    @Test
    fun testFinishOnboardingCompletesAndSavesCompanion() = runTest(testDispatcher) {
        screenModel.handleIntent(OnboardingIntent.SetCompanionName("Sage"))
        screenModel.handleIntent(OnboardingIntent.FinishOnboarding)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(screenModel.state.value.isCompleted)
        val saved = fakeCompanionDao.getCompanionById("primary")
        assertEquals("Sage", saved?.name)
    }
}
