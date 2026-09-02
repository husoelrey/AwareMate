package org.awaremate.shared.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.awaremate.shared.data.local.entity.CompanionEntity
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.data.repository.DailyChallengeRepositoryImpl
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.MomentumTier
import org.awaremate.shared.domain.usecase.awareness.CalculateAwarenessScoreUseCase
import org.awaremate.shared.domain.usecase.challenge.CompleteDailyChallengeUseCase
import org.awaremate.shared.domain.usecase.challenge.GenerateDailyChallengesUseCase
import org.awaremate.shared.domain.usecase.challenge.GetDailyChallengesUseCase
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.CalculateGrowthStageUseCase
import org.awaremate.shared.domain.usecase.companion.GetCompanionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase
import org.awaremate.shared.presentation.home.HomeIntent
import org.awaremate.shared.presentation.home.HomeScreenModel
import org.awaremate.shared.test.FakeCompanionDao
import org.awaremate.shared.test.FakeDailyChallengeDao
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCompanionDao: FakeCompanionDao
    private lateinit var fakeDailyChallengeDao: FakeDailyChallengeDao
    private lateinit var screenModel: HomeScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCompanionDao = FakeCompanionDao()
        fakeDailyChallengeDao = FakeDailyChallengeDao()

        val companionRepo = CompanionRepositoryImpl(companionDao = fakeCompanionDao)
        val challengeRepo = DailyChallengeRepositoryImpl(dailyChallengeDao = fakeDailyChallengeDao)

        val calculateGrowthStageUseCase = CalculateGrowthStageUseCase()
        val addExperienceUseCase = AddExperienceUseCase(companionRepo)
        val updateMomentumUseCase = UpdateMomentumUseCase(companionRepo)
        val updateCompanionEmotionUseCase = UpdateCompanionEmotionUseCase(companionRepo)
        val getCompanionUseCase = GetCompanionUseCase(companionRepo)
        val generateDailyChallengesUseCase = GenerateDailyChallengesUseCase(challengeRepo)
        val getDailyChallengesUseCase = GetDailyChallengesUseCase(challengeRepo)
        val completeDailyChallengeUseCase = CompleteDailyChallengeUseCase(challengeRepo, addExperienceUseCase)
        val calculateAwarenessScoreUseCase = CalculateAwarenessScoreUseCase()

        screenModel = HomeScreenModel(
            getCompanionUseCase = getCompanionUseCase,
            calculateGrowthStageUseCase = calculateGrowthStageUseCase,
            calculateAwarenessScoreUseCase = calculateAwarenessScoreUseCase,
            getDailyChallengesUseCase = getDailyChallengesUseCase,
            generateDailyChallengesUseCase = generateDailyChallengesUseCase,
            completeDailyChallengeUseCase = completeDailyChallengeUseCase,
            addExperienceUseCase = addExperienceUseCase,
            updateMomentumUseCase = updateMomentumUseCase,
            updateCompanionEmotionUseCase = updateCompanionEmotionUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDashboardLoadsCompanionAndCalculatesMetrics() = runTest(testDispatcher) {
        fakeCompanionDao.insertCompanion(
            CompanionEntity.fromDomain(
                org.awaremate.shared.domain.model.Companion(
                    id = "primary",
                    name = "Sprout",
                    stage = CompanionStage.SEED,
                    emotion = CompanionEmotion.PEACEFUL,
                    experiencePoints = 50,
                    momentumScore = 90.0,
                    happinessXp = 20,
                    energyXp = 10,
                    wisdomXp = 10,
                    creativityXp = 10
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertEquals("Sprout", state.companion.name)
        assertEquals(50, state.companion.experiencePoints)
        assertEquals(MomentumTier.SPARKING, state.momentumTier)
        assertNotNull(state.growthMetrics)
    }

    @Test
    fun testWaterPlantBoostsXpAndShowsSnackbar() = runTest(testDispatcher) {
        fakeCompanionDao.insertCompanion(
            CompanionEntity.fromDomain(
                org.awaremate.shared.domain.model.Companion(
                    id = "primary",
                    name = "Sprout",
                    stage = CompanionStage.SEED,
                    emotion = CompanionEmotion.PEACEFUL,
                    experiencePoints = 0,
                    momentumScore = 50.0
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.handleIntent(HomeIntent.WaterPlant)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertNotNull(state.snackbarMessage)
        assertTrue(state.snackbarMessage!!.contains("Watered Sprout!"))
    }

    @Test
    fun testCompleteChallengeAwardsXp() = runTest(testDispatcher) {
        fakeCompanionDao.insertCompanion(
            CompanionEntity.fromDomain(
                org.awaremate.shared.domain.model.Companion(
                    id = "primary",
                    name = "Sprout",
                    stage = CompanionStage.SEED,
                    emotion = CompanionEmotion.PEACEFUL,
                    experiencePoints = 0,
                    momentumScore = 50.0
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val challenge = DailyChallenge(
            id = "c1",
            userId = "u1",
            title = "Mindful Breath",
            description = "Breathe deeply",
            category = CompanionCategory.WISDOM,
            xpReward = 20,
            dateString = "2026-09-02",
            completed = false
        )

        screenModel.handleIntent(HomeIntent.CompleteChallenge(challenge))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertNotNull(state.snackbarMessage)
        assertTrue(state.snackbarMessage!!.contains("Challenge completed!"))
    }
}
