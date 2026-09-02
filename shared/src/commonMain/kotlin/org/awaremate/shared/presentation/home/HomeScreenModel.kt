package org.awaremate.shared.presentation.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEvent
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.MomentumCalculator
import org.awaremate.shared.domain.usecase.awareness.CalculateAwarenessScoreUseCase
import org.awaremate.shared.domain.usecase.challenge.CompleteDailyChallengeUseCase
import org.awaremate.shared.domain.usecase.challenge.GenerateDailyChallengesUseCase
import org.awaremate.shared.domain.usecase.challenge.GetDailyChallengesUseCase
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.CalculateGrowthStageUseCase
import org.awaremate.shared.domain.usecase.companion.GetCompanionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase

class HomeScreenModel(
    private val getCompanionUseCase: GetCompanionUseCase,
    private val calculateGrowthStageUseCase: CalculateGrowthStageUseCase,
    private val calculateAwarenessScoreUseCase: CalculateAwarenessScoreUseCase,
    private val getDailyChallengesUseCase: GetDailyChallengesUseCase,
    private val generateDailyChallengesUseCase: GenerateDailyChallengesUseCase,
    private val completeDailyChallengeUseCase: CompleteDailyChallengeUseCase,
    private val addExperienceUseCase: AddExperienceUseCase,
    private val updateMomentumUseCase: UpdateMomentumUseCase,
    private val updateCompanionEmotionUseCase: UpdateCompanionEmotionUseCase,
    private val digitalSunsetUseCase: org.awaremate.shared.domain.usecase.sunset.DigitalSunsetUseCase? = null
) : ScreenModel {

    private val _state = MutableStateFlow(HomeState(isLoading = true))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        screenModelScope.launch {
            val todayStr = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

            // Ensure today's challenges exist
            generateDailyChallengesUseCase(todayStr)

            // Observe companion
            launch {
                getCompanionUseCase().collect { companionOrNull ->
                    val comp = companionOrNull ?: Companion()
                    val metrics = calculateGrowthStageUseCase(comp)
                    val tier = MomentumCalculator.getTierForScore(comp.momentumScore)

                    _state.update {
                        it.copy(
                            companion = comp,
                            growthMetrics = metrics,
                            momentumTier = tier,
                            isLoading = false
                        )
                    }
                }
            }

            // Observe daily challenges
            launch {
                getDailyChallengesUseCase(todayStr).collect { challenges ->
                    _state.update { it.copy(dailyChallenges = challenges) }
                }
            }

            // Observe digital sunset status
            digitalSunsetUseCase?.let { sunsetUseCase ->
                launch {
                    val status = sunsetUseCase.getStatus()
                    _state.update { it.copy(sunsetStatus = status) }
                }
            }

            // Calculate awareness score
            val awareness = calculateAwarenessScoreUseCase(
                screenTimeMinutes = 120,
                targetScreenTimeGoalMinutes = 180,
                focusSessionMinutes = 25,
                moodEntriesCount = 1,
                completedChallengesCount = 1
            )
            _state.update { it.copy(awarenessScore = awareness) }
        }
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadDashboard -> loadDashboardData()

            HomeIntent.WaterPlant -> {
                screenModelScope.launch {
                    val result = addExperienceUseCase(amount = 10, category = CompanionCategory.HAPPINESS)
                    if (result.isSuccess) {
                        updateCompanionEmotionUseCase(CompanionEvent.BreathExerciseCompleted)
                        updateMomentumUseCase.boostMomentum(MomentumCalculator.BASE_ACTIVITY_BOOST)
                        _state.update {
                            it.copy(snackbarMessage = "Watered ${_state.value.companion.name}! +10 XP & Momentum boosted 🌱")
                        }
                    }
                }
            }

            is HomeIntent.CompleteChallenge -> {
                screenModelScope.launch {
                    val result = completeDailyChallengeUseCase(intent.challenge)
                    if (result.isSuccess) {
                        _state.update {
                            it.copy(snackbarMessage = "Challenge completed! Well done! +${intent.challenge.xpReward} XP ⭐")
                        }
                    }
                }
            }

            HomeIntent.ClearSnackbar -> {
                _state.update { it.copy(snackbarMessage = null) }
            }
        }
    }
}
