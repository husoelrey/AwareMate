package org.awaremate.shared.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.awaremate.shared.data.local.entity.CompanionEntity
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.data.repository.FocusSessionRepositoryImpl
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.model.FocusCategory
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.CalculateGrowthStageUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase
import org.awaremate.shared.presentation.focus.FocusIntent
import org.awaremate.shared.presentation.focus.FocusScreenModel
import org.awaremate.shared.presentation.focus.FocusTimerStatus
import org.awaremate.shared.test.FakeCompanionDao
import org.awaremate.shared.test.FakeFocusSessionDao
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestClock(var currentEpochMs: Long = 1_000_000_000L) : kotlinx.datetime.Clock {
    override fun now(): kotlinx.datetime.Instant = kotlinx.datetime.Instant.fromEpochMilliseconds(currentEpochMs)
    fun advanceBy(ms: Long) {
        currentEpochMs += ms
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FocusScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeFocusSessionDao: FakeFocusSessionDao
    private lateinit var fakeCompanionDao: FakeCompanionDao
    private lateinit var testClock: TestClock
    private lateinit var screenModel: FocusScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeFocusSessionDao = FakeFocusSessionDao()
        fakeCompanionDao = FakeCompanionDao()
        testClock = TestClock()

        val focusRepo = FocusSessionRepositoryImpl(focusSessionDao = fakeFocusSessionDao)
        val companionRepo = CompanionRepositoryImpl(companionDao = fakeCompanionDao)
        val addExperienceUseCase = AddExperienceUseCase(companionRepo)
        val updateMomentumUseCase = UpdateMomentumUseCase(companionRepo)

        screenModel = FocusScreenModel(
            focusSessionRepository = focusRepo,
            companionRepository = companionRepo,
            addExperienceUseCase = addExperienceUseCase,
            updateMomentumUseCase = updateMomentumUseCase,
            calculateGrowthStageUseCase = CalculateGrowthStageUseCase(),
            clock = testClock
        )
    }

    @AfterTest
    fun tearDown() {
        screenModel.onDispose()
        Dispatchers.resetMain()
    }

    @Test
    fun testSelectDurationAndCategoryWhenIdle() = runTest(testDispatcher) {
        screenModel.handleIntent(FocusIntent.SelectDuration(45))
        screenModel.handleIntent(FocusIntent.SelectCategory(FocusCategory.STUDY))

        val state = screenModel.state.value
        assertEquals(45, state.selectedDurationMinutes)
        assertEquals(45 * 60, state.remainingSeconds)
        assertEquals(FocusCategory.STUDY, state.selectedCategory)
    }

    @Test
    fun testStartPauseResumeAndStopCycle() = runTest(testDispatcher) {
        // Initial state
        assertEquals(FocusTimerStatus.IDLE, screenModel.state.value.status)

        // Start
        screenModel.handleIntent(FocusIntent.StartTimer)
        assertEquals(FocusTimerStatus.RUNNING, screenModel.state.value.status)
        assertEquals(CompanionEmotion.PEACEFUL, screenModel.state.value.companionEmotion)

        // Pause
        screenModel.handleIntent(FocusIntent.PauseTimer)
        assertEquals(FocusTimerStatus.PAUSED, screenModel.state.value.status)
        assertEquals(CompanionEmotion.CURIOUS, screenModel.state.value.companionEmotion)

        // Resume
        screenModel.handleIntent(FocusIntent.ResumeTimer)
        assertEquals(FocusTimerStatus.RUNNING, screenModel.state.value.status)
        assertEquals(CompanionEmotion.PEACEFUL, screenModel.state.value.companionEmotion)

        // Stop
        screenModel.handleIntent(FocusIntent.StopTimer)
        assertEquals(FocusTimerStatus.IDLE, screenModel.state.value.status)
        assertEquals(screenModel.state.value.selectedDurationMinutes * 60, screenModel.state.value.remainingSeconds)
    }

    @Test
    fun testSessionCompletionAwardsXpAndBoostsCompanion() = runTest(testDispatcher) {
        // Seed companion
        fakeCompanionDao.insertCompanion(
            CompanionEntity.fromDomain(
                Companion(
                    id = "primary",
                    name = "Sprout",
                    stage = CompanionStage.SPROUT,
                    experiencePoints = 10,
                    momentumScore = 50.0
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Select 15 min session
        screenModel.handleIntent(FocusIntent.SelectDuration(15))
        screenModel.handleIntent(FocusIntent.StartTimer)

        // Advance past all remaining seconds via test clock
        testClock.advanceBy(16 * 60 * 1000L)
        testDispatcher.scheduler.advanceTimeBy(1000L)
        testDispatcher.scheduler.runCurrent()

        val state = screenModel.state.value
        assertEquals(FocusTimerStatus.COMPLETED, state.status)
        assertEquals(CompanionEmotion.CHEERFUL, state.companionEmotion)
        assertTrue(state.showCelebrationDialog)
        assertEquals(30, state.earnedXp) // 15 * 2 = 30 XP
        assertEquals(15L, state.totalFocusMinutesToday)

        // Verify session saved in DB
        val sessions = fakeFocusSessionDao.getUnsyncedSessions()
        assertEquals(1, sessions.size)
        assertEquals(15 * 60, sessions.first().durationSeconds)

        // Dismiss celebration
        screenModel.handleIntent(FocusIntent.DismissCelebration)
        assertFalse(screenModel.state.value.showCelebrationDialog)
        assertEquals(FocusTimerStatus.IDLE, screenModel.state.value.status)
    }

    @Test
    fun testTimerBackgroundResilienceViaTargetEndTime() = runTest(testDispatcher) {
        screenModel.handleIntent(FocusIntent.SelectDuration(25))
        screenModel.handleIntent(FocusIntent.StartTimer)

        val stateRunning = screenModel.state.value
        assertEquals(FocusTimerStatus.RUNNING, stateRunning.status)
        kotlin.test.assertNotNull(stateRunning.targetEndTimeEpochMs)
        val initialTarget = stateRunning.targetEndTimeEpochMs!!
        assertTrue(initialTarget > 0)

        // Pause clears targetEndTime and freezes remaining seconds
        screenModel.handleIntent(FocusIntent.PauseTimer)
        assertEquals(FocusTimerStatus.PAUSED, screenModel.state.value.status)
        assertEquals(null, screenModel.state.value.targetEndTimeEpochMs)

        // Resume recomputes targetEndTime
        screenModel.handleIntent(FocusIntent.ResumeTimer)
        assertEquals(FocusTimerStatus.RUNNING, screenModel.state.value.status)
        kotlin.test.assertNotNull(screenModel.state.value.targetEndTimeEpochMs)

        // Stop timer to clean up coroutine
        screenModel.handleIntent(FocusIntent.StopTimer)
        assertEquals(FocusTimerStatus.IDLE, screenModel.state.value.status)
    }

    @Test
    fun testTimerBackgroundExecutionAccuratelyAccountsForElapsedTime() = runTest(testDispatcher) {
        // Start 25-minute timer (1500 seconds)
        screenModel.handleIntent(FocusIntent.SelectDuration(25))
        screenModel.handleIntent(FocusIntent.StartTimer)

        assertEquals(FocusTimerStatus.RUNNING, screenModel.state.value.status)
        assertEquals(1500, screenModel.state.value.remainingSeconds)

        // Simulate app minimized for 10 minutes (clock advances by 10 minutes)
        testClock.advanceBy(10 * 60 * 1000L)
        testDispatcher.scheduler.advanceTimeBy(1000L)
        testDispatcher.scheduler.runCurrent()

        // Verify remaining seconds adjusted without drift: exactly 15 minutes left (900 seconds)
        assertEquals(FocusTimerStatus.RUNNING, screenModel.state.value.status)
        assertEquals(15 * 60, screenModel.state.value.remainingSeconds)

        // Simulate app remaining minimized until after the timer expires (16 more minutes, total 26 min)
        testClock.advanceBy(16 * 60 * 1000L)
        testDispatcher.scheduler.advanceTimeBy(1000L)
        testDispatcher.scheduler.runCurrent()

        // Timer must automatically complete, award XP, and save session
        assertEquals(FocusTimerStatus.COMPLETED, screenModel.state.value.status)
        assertEquals(0, screenModel.state.value.remainingSeconds)
        assertTrue(screenModel.state.value.showCelebrationDialog)
        assertEquals(50, screenModel.state.value.earnedXp) // 25 * 2 = 50 XP

        val savedSessions = fakeFocusSessionDao.getUnsyncedSessions()
        assertTrue(savedSessions.any { it.durationSeconds == 25 * 60 })
    }
}
