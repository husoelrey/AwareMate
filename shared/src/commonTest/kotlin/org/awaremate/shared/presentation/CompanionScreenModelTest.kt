package org.awaremate.shared.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.awaremate.shared.data.local.entity.CompanionEntity
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.CalculateGrowthStageUseCase
import org.awaremate.shared.domain.usecase.companion.GetCompanionUseCase
import org.awaremate.shared.domain.usecase.companion.SaveCompanionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase
import org.awaremate.shared.presentation.companion.CompanionIntent
import org.awaremate.shared.presentation.companion.CompanionScreenModel
import org.awaremate.shared.test.FakeCompanionDao
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CompanionScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCompanionDao: FakeCompanionDao
    private lateinit var screenModel: CompanionScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCompanionDao = FakeCompanionDao()
        val companionRepo = CompanionRepositoryImpl(companionDao = fakeCompanionDao)
        val calculateGrowthStageUseCase = CalculateGrowthStageUseCase()
        val addExperienceUseCase = AddExperienceUseCase(companionRepo)
        val updateCompanionEmotionUseCase = UpdateCompanionEmotionUseCase(companionRepo)
        val getCompanionUseCase = GetCompanionUseCase(companionRepo)
        val saveCompanionUseCase = SaveCompanionUseCase(companionRepo)

        screenModel = CompanionScreenModel(
            getCompanionUseCase = getCompanionUseCase,
            calculateGrowthStageUseCase = calculateGrowthStageUseCase,
            addExperienceUseCase = addExperienceUseCase,
            updateCompanionEmotionUseCase = updateCompanionEmotionUseCase,
            saveCompanionUseCase = saveCompanionUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCompanionLoadsWithGrowthMetrics() = runTest(testDispatcher) {
        fakeCompanionDao.insertCompanion(
            CompanionEntity.fromDomain(
                org.awaremate.shared.domain.model.Companion(
                    id = "primary",
                    name = "Sprout",
                    stage = CompanionStage.SEED,
                    emotion = CompanionEmotion.PEACEFUL,
                    experiencePoints = 60,
                    momentumScore = 100.0,
                    happinessXp = 30,
                    energyXp = 10,
                    wisdomXp = 10,
                    creativityXp = 10
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertEquals("Sprout", state.companion.name)
        assertEquals(60, state.companion.experiencePoints)
        assertNotNull(state.growthMetrics)
        assertEquals(CompanionStage.SEED, state.growthMetrics!!.stage)
        assertEquals(CompanionStage.SPROUT, state.growthMetrics!!.nextStage)
    }

    @Test
    fun testWaterCompanionAddsXpAndSetsMessage() = runTest(testDispatcher) {
        fakeCompanionDao.insertCompanion(
            CompanionEntity.fromDomain(
                org.awaremate.shared.domain.model.Companion(
                    id = "primary",
                    name = "Sprout",
                    stage = CompanionStage.SEED,
                    emotion = CompanionEmotion.PEACEFUL,
                    experiencePoints = 0,
                    momentumScore = 100.0
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.handleIntent(CompanionIntent.WaterCompanion(CompanionCategory.HAPPINESS))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = screenModel.state.value
        assertNotNull(state.interactionMessage)
        assertTrue(state.interactionMessage!!.contains("You watered Sprout!"))
    }

    @Test
    fun testRenameCompanion() = runTest(testDispatcher) {
        fakeCompanionDao.insertCompanion(
            CompanionEntity.fromDomain(
                org.awaremate.shared.domain.model.Companion(
                    id = "primary",
                    name = "Sprout",
                    stage = CompanionStage.SEED,
                    emotion = CompanionEmotion.PEACEFUL,
                    experiencePoints = 0,
                    momentumScore = 100.0
                )
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.handleIntent(CompanionIntent.RenameCompanion("Willow"))
        testDispatcher.scheduler.advanceUntilIdle()

        val saved = fakeCompanionDao.getCompanionById("primary")
        assertEquals("Willow", saved?.name)
    }
}
