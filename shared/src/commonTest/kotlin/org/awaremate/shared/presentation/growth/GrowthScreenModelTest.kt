package org.awaremate.shared.presentation.growth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.data.repository.DailyChallengeRepositoryImpl
import org.awaremate.shared.data.repository.HobbyRepositoryImpl
import org.awaremate.shared.data.repository.MoodRepositoryImpl
import org.awaremate.shared.data.repository.SelfDiscoveryRepositoryImpl
import org.awaremate.shared.domain.model.BreathingPattern
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCategory
import org.awaremate.shared.domain.model.HobbyEnergyLevel
import org.awaremate.shared.domain.usecase.challenge.CompleteDailyChallengeUseCase
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase
import org.awaremate.shared.domain.usecase.growth.GetPersonalizedHobbiesUseCase
import org.awaremate.shared.domain.usecase.growth.GetWeeklyMoodInsightsUseCase
import org.awaremate.shared.domain.usecase.growth.LogMoodUseCase
import org.awaremate.shared.test.FakeCompanionDao
import org.awaremate.shared.test.FakeDailyChallengeDao
import org.awaremate.shared.test.FakeHobbyDao
import org.awaremate.shared.test.FakeMoodEntryDao
import org.awaremate.shared.test.FakeSelfDiscoveryPromptDao
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GrowthScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var moodDao: FakeMoodEntryDao
    private lateinit var moodRepo: MoodRepositoryImpl
    private lateinit var hobbyDao: FakeHobbyDao
    private lateinit var hobbyRepo: HobbyRepositoryImpl
    private lateinit var promptDao: FakeSelfDiscoveryPromptDao
    private lateinit var promptRepo: SelfDiscoveryRepositoryImpl
    private lateinit var challengeDao: FakeDailyChallengeDao
    private lateinit var challengeRepo: DailyChallengeRepositoryImpl
    private lateinit var companionDao: FakeCompanionDao
    private lateinit var companionRepo: CompanionRepositoryImpl

    private lateinit var addExpUseCase: AddExperienceUseCase
    private lateinit var momentumUseCase: UpdateMomentumUseCase
    private lateinit var emotionUseCase: UpdateCompanionEmotionUseCase
    private lateinit var completeChallengeUseCase: CompleteDailyChallengeUseCase
    private lateinit var logMoodUseCase: LogMoodUseCase

    private lateinit var screenModel: GrowthScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        moodDao = FakeMoodEntryDao()
        moodRepo = MoodRepositoryImpl(moodDao)
        hobbyDao = FakeHobbyDao()
        hobbyRepo = HobbyRepositoryImpl(hobbyDao)
        promptDao = FakeSelfDiscoveryPromptDao()
        promptRepo = SelfDiscoveryRepositoryImpl(promptDao)
        challengeDao = FakeDailyChallengeDao()
        challengeRepo = DailyChallengeRepositoryImpl(challengeDao)
        companionDao = FakeCompanionDao()
        companionRepo = CompanionRepositoryImpl(companionDao)

        addExpUseCase = AddExperienceUseCase(companionRepo)
        momentumUseCase = UpdateMomentumUseCase(companionRepo)
        emotionUseCase = UpdateCompanionEmotionUseCase(companionRepo)
        completeChallengeUseCase = CompleteDailyChallengeUseCase(challengeRepo, addExpUseCase)
        logMoodUseCase = LogMoodUseCase(
            moodRepository = moodRepo,
            addExperienceUseCase = addExpUseCase,
            updateMomentumUseCase = momentumUseCase,
            updateCompanionEmotionUseCase = emotionUseCase
        )

        screenModel = GrowthScreenModel(
            moodRepository = moodRepo,
            hobbyRepository = hobbyRepo,
            selfDiscoveryRepository = promptRepo,
            dailyChallengeRepository = challengeRepo,
            logMoodUseCase = logMoodUseCase,
            getPersonalizedHobbiesUseCase = GetPersonalizedHobbiesUseCase(),
            getWeeklyMoodInsightsUseCase = GetWeeklyMoodInsightsUseCase(),
            addExperienceUseCase = addExpUseCase,
            updateMomentumUseCase = momentumUseCase,
            updateCompanionEmotionUseCase = emotionUseCase,
            completeDailyChallengeUseCase = completeChallengeUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitializationLoadsDefaultCatalogsAndData() = runTest {
        advanceUntilIdle()

        val state = screenModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.allHobbies.isNotEmpty())
        assertTrue(state.prompts.isNotEmpty())
        assertNotNull(state.currentPrompt)
    }

    @Test
    fun testSubmitMoodUpdatesStateAndAwardsWisdomXp() = runTest {
        advanceUntilIdle()

        screenModel.handleIntent(
            GrowthIntent.SubmitMood(
                emoji = "😄",
                moodScore = 5,
                energyLevel = 4,
                note = "Had an inspiring afternoon",
                tags = listOf("Creative", "Screen-Free")
            )
        )
        advanceUntilIdle()

        val state = screenModel.state.value
        assertFalse(state.isMoodDialogOpen)
        assertEquals(1, state.recentMoods.size)
        assertEquals("😄", state.recentMoods.first().emoji)
        assertNotNull(state.snackbarMessage)

        val companion = companionRepo.getCompanion().first()
        assertEquals(15, companion?.wisdomXp)
        assertEquals(CompanionEmotion.PEACEFUL, companion?.emotion)
    }

    @Test
    fun testPromptNavigationAndAcknowledgment() = runTest {
        advanceUntilIdle()

        val initialPrompt = screenModel.state.value.currentPrompt
        assertNotNull(initialPrompt)

        // Next prompt
        screenModel.handleIntent(GrowthIntent.NextSelfDiscoveryPrompt)
        val nextPrompt = screenModel.state.value.currentPrompt
        assertNotNull(nextPrompt)
        assertTrue(initialPrompt.id != nextPrompt.id)

        // Acknowledge prompt
        screenModel.handleIntent(
            GrowthIntent.AcknowledgeSelfDiscovery(
                promptId = nextPrompt.id,
                reflection = "Noticed this reflex when standing in queue"
            )
        )
        advanceUntilIdle()

        val companion = companionRepo.getCompanion().first()
        assertEquals(15, companion?.wisdomXp)
    }

    @Test
    fun testCompleteHobbySessionAwardsCreativityXp() = runTest {
        advanceUntilIdle()

        val hobby = Hobby(
            id = "hobby_clay",
            title = "Clay Sculpting",
            category = HobbyCategory.HANDS_ON_CRAFT,
            description = "Sculpting miniature clay",
            beginnerTip = "Tip",
            energyLevel = HobbyEnergyLevel.MODERATE
        )

        screenModel.handleIntent(GrowthIntent.CompleteHobbySession(hobby))
        advanceUntilIdle()

        val companion = companionRepo.getCompanion().first()
        assertEquals(25, companion?.creativityXp)
        assertNotNull(screenModel.state.value.snackbarMessage)
    }

    @Test
    fun testBreathingSessionStateTransitions() = runTest {
        screenModel.handleIntent(
            GrowthIntent.StartBreathing(
                pattern = BreathingPattern.GROUNDING_CALM,
                targetCycles = 2
            )
        )

        val activeState = screenModel.state.value.breathingState
        assertTrue(activeState.isActive)
        assertFalse(activeState.isPaused)
        assertEquals(BreathingPattern.GROUNDING_CALM, activeState.pattern)

        screenModel.handleIntent(GrowthIntent.PauseBreathing)
        assertTrue(screenModel.state.value.breathingState.isPaused)

        screenModel.handleIntent(GrowthIntent.ResumeBreathing)
        assertFalse(screenModel.state.value.breathingState.isPaused)

        screenModel.handleIntent(GrowthIntent.StopBreathing)
        assertFalse(screenModel.state.value.breathingState.isActive)
    }
}
