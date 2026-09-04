package org.awaremate.shared.presentation.growth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.awaremate.shared.domain.model.BreathingPattern
import org.awaremate.shared.domain.model.BreathingPhase
import org.awaremate.shared.domain.model.BreathingSessionState
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEvent
import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.repository.DailyChallengeRepository
import org.awaremate.shared.domain.repository.HobbyRepository
import org.awaremate.shared.domain.repository.MoodRepository
import org.awaremate.shared.domain.repository.SelfDiscoveryRepository
import org.awaremate.shared.domain.usecase.challenge.CompleteDailyChallengeUseCase
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase
import org.awaremate.shared.domain.usecase.growth.GetPersonalizedHobbiesUseCase
import org.awaremate.shared.domain.usecase.growth.GetWeeklyMoodInsightsUseCase
import org.awaremate.shared.domain.usecase.growth.LogMoodUseCase

class GrowthScreenModel(
    private val moodRepository: MoodRepository,
    private val hobbyRepository: HobbyRepository,
    private val selfDiscoveryRepository: SelfDiscoveryRepository,
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val logMoodUseCase: LogMoodUseCase,
    private val getPersonalizedHobbiesUseCase: GetPersonalizedHobbiesUseCase,
    private val getWeeklyMoodInsightsUseCase: GetWeeklyMoodInsightsUseCase,
    private val addExperienceUseCase: AddExperienceUseCase,
    private val updateMomentumUseCase: UpdateMomentumUseCase,
    private val updateCompanionEmotionUseCase: UpdateCompanionEmotionUseCase,
    private val completeDailyChallengeUseCase: CompleteDailyChallengeUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(GrowthState())
    val state: StateFlow<GrowthState> = _state.asStateFlow()

    private var breathingJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        screenModelScope.launch {
            // Initialize default seed catalogs if needed
            hobbyRepository.initializeDefaultHobbies()
            selfDiscoveryRepository.initializeDefaultPrompts()

            val todayStr = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

            // 1. Observe mood entries
            launch {
                moodRepository.getAllMoodEntries().collect { moods ->
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    val todayMood = moods.firstOrNull {
                        // Check if entry timestamp is within today
                        val entryDate = kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestampEpochMs)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        entryDate == today
                    }
                    val insights = getWeeklyMoodInsightsUseCase(moods)

                    _state.update { current ->
                        current.copy(
                            moodEntries = moods,
                            recentMoods = moods.take(10),
                            todayMood = todayMood,
                            weeklyInsights = insights
                        )
                    }
                    updateRecommendations()
                }
            }

            // 2. Observe hobbies
            launch {
                hobbyRepository.getAllHobbies().collect { hobbies ->
                    _state.update { it.copy(allHobbies = hobbies) }
                    updateRecommendations()
                }
            }

            // 3. Observe self-discovery prompts
            launch {
                selfDiscoveryRepository.getAllPrompts().collect { prompts ->
                    _state.update { it.copy(prompts = prompts) }
                }
            }

            // 4. Observe daily challenges
            launch {
                dailyChallengeRepository.getChallengesForDate(todayStr).collect { challenges ->
                    _state.update { it.copy(dailyChallenges = challenges, isLoading = false) }
                }
            }
        }
    }

    private fun updateRecommendations() {
        val current = _state.value
        val latestEnergy = current.todayMood?.energyLevel ?: current.recentMoods.firstOrNull()?.energyLevel
        val recommended = getPersonalizedHobbiesUseCase(
            allHobbies = current.allHobbies,
            currentEnergyLevel = latestEnergy,
            preferredCategory = current.selectedHobbyCategory,
            limit = 6
        )
        _state.update { it.copy(recommendedHobbies = recommended) }
    }

    fun handleIntent(intent: GrowthIntent) {
        when (intent) {
            GrowthIntent.LoadGrowthData -> loadData()

            GrowthIntent.OpenMoodDialog -> {
                _state.update { it.copy(isMoodDialogOpen = true) }
            }

            GrowthIntent.DismissMoodDialog -> {
                _state.update { it.copy(isMoodDialogOpen = false) }
            }

            is GrowthIntent.SubmitMood -> {
                screenModelScope.launch {
                    val now = Clock.System.now().toEpochMilliseconds()
                    val entry = MoodEntry(
                        id = "mood_$now",
                        userId = "primary",
                        timestampEpochMs = now,
                        emoji = intent.emoji,
                        moodScore = intent.moodScore,
                        energyLevel = intent.energyLevel,
                        note = intent.note,
                        tags = intent.tags
                    )
                    val result = logMoodUseCase(entry)
                    if (result.isSuccess) {
                        _state.update {
                            it.copy(
                                isMoodDialogOpen = false,
                                snackbarMessage = "Mood recorded! Companion is feeling peaceful 🌱 +15 XP"
                            )
                        }
                    }
                }
            }

            GrowthIntent.OpenBreathingDialog -> {
                _state.update { it.copy(isBreathingDialogOpen = true) }
            }

            GrowthIntent.DismissBreathingDialog -> {
                stopBreathingTimer()
                _state.update { it.copy(isBreathingDialogOpen = false) }
            }

            is GrowthIntent.StartBreathing -> {
                startBreathingSession(intent.pattern, intent.targetCycles)
            }

            GrowthIntent.PauseBreathing -> {
                _state.update { it.copy(breathingState = it.breathingState.copy(isPaused = true)) }
            }

            GrowthIntent.ResumeBreathing -> {
                _state.update { it.copy(breathingState = it.breathingState.copy(isPaused = false)) }
            }

            GrowthIntent.StopBreathing -> {
                stopBreathingTimer()
            }

            is GrowthIntent.ToggleHobbyBookmark -> {
                screenModelScope.launch {
                    hobbyRepository.toggleBookmark(intent.hobbyId, intent.isBookmarked)
                }
            }

            is GrowthIntent.CompleteHobbySession -> {
                screenModelScope.launch {
                    val result = hobbyRepository.completeHobbySession(intent.hobby.id)
                    if (result.isSuccess) {
                        addExperienceUseCase(category = CompanionCategory.CREATIVITY, amount = 25)
                        updateMomentumUseCase.boostMomentum(2.0)
                        _state.update {
                            it.copy(snackbarMessage = "Dedicated offline time to ${intent.hobby.title}! +25 Creativity XP ✨")
                        }
                    }
                }
            }

            is GrowthIntent.SelectHobbyCategory -> {
                _state.update { it.copy(selectedHobbyCategory = intent.category) }
                updateRecommendations()
            }

            GrowthIntent.NextSelfDiscoveryPrompt -> {
                _state.update { current ->
                    if (current.prompts.isNotEmpty()) {
                        val nextIndex = (current.currentPromptIndex + 1) % current.prompts.size
                        current.copy(currentPromptIndex = nextIndex)
                    } else current
                }
            }

            GrowthIntent.PreviousSelfDiscoveryPrompt -> {
                _state.update { current ->
                    if (current.prompts.isNotEmpty()) {
                        val prevIndex = if (current.currentPromptIndex > 0) {
                            current.currentPromptIndex - 1
                        } else current.prompts.size - 1
                        current.copy(currentPromptIndex = prevIndex)
                    } else current
                }
            }

            is GrowthIntent.AcknowledgeSelfDiscovery -> {
                screenModelScope.launch {
                    val result = selfDiscoveryRepository.recordObservation(intent.promptId, intent.reflection)
                    if (result.isSuccess) {
                        addExperienceUseCase(category = CompanionCategory.WISDOM, amount = 15)
                        updateMomentumUseCase.boostMomentum(1.5)
                        _state.update {
                            it.copy(snackbarMessage = "Observation acknowledged! Curiosity nurtures self-awareness 💡 +15 XP")
                        }
                    }
                }
            }

            is GrowthIntent.CompleteChallenge -> {
                screenModelScope.launch {
                    val result = completeDailyChallengeUseCase(intent.challenge)
                    if (result.isSuccess) {
                        _state.update {
                            it.copy(snackbarMessage = "Challenge completed! +${intent.challenge.xpReward} XP ⭐")
                        }
                    }
                }
            }

            GrowthIntent.ClearSnackbar -> {
                _state.update { it.copy(snackbarMessage = null) }
            }
        }
    }

    private fun startBreathingSession(pattern: BreathingPattern, targetCycles: Int) {
        breathingJob?.cancel()
        _state.update {
            it.copy(
                breathingState = BreathingSessionState(
                    pattern = pattern,
                    currentPhase = BreathingPhase.INHALE,
                    secondsRemainingInPhase = pattern.inhaleSeconds,
                    phaseProgress = 0f,
                    currentCycle = 1,
                    targetCycles = targetCycles,
                    isActive = true,
                    isPaused = false,
                    isCompleted = false
                )
            )
        }

        breathingJob = screenModelScope.launch {
            for (cycle in 1..targetCycles) {
                // Phase 1: Inhale
                runPhase(BreathingPhase.INHALE, pattern.inhaleSeconds, cycle, targetCycles, pattern)

                // Phase 2: Hold In (if duration > 0)
                if (pattern.holdInSeconds > 0) {
                    runPhase(BreathingPhase.HOLD_IN, pattern.holdInSeconds, cycle, targetCycles, pattern)
                }

                // Phase 3: Exhale
                runPhase(BreathingPhase.EXHALE, pattern.exhaleSeconds, cycle, targetCycles, pattern)

                // Phase 4: Hold Out (if duration > 0)
                if (pattern.holdOutSeconds > 0) {
                    runPhase(BreathingPhase.HOLD_OUT, pattern.holdOutSeconds, cycle, targetCycles, pattern)
                }
            }

            // Session completed!
            _state.update {
                it.copy(
                    breathingState = it.breathingState.copy(
                        isActive = false,
                        isCompleted = true
                    )
                )
            }

            addExperienceUseCase(category = CompanionCategory.ENERGY, amount = 20)
            updateCompanionEmotionUseCase(CompanionEvent.BreathExerciseCompleted)
            updateMomentumUseCase.boostMomentum(1.5)
            _state.update {
                it.copy(snackbarMessage = "Mindful breathing completed! Companion is peaceful 🌱 +20 Energy XP")
            }
        }
    }

    private suspend fun runPhase(
        phase: BreathingPhase,
        durationSeconds: Int,
        cycle: Int,
        targetCycles: Int,
        pattern: BreathingPattern
    ) {
        for (second in durationSeconds downTo 1) {
            while (_state.value.breathingState.isPaused) {
                delay(100)
            }
            _state.update {
                it.copy(
                    breathingState = it.breathingState.copy(
                        pattern = pattern,
                        currentPhase = phase,
                        secondsRemainingInPhase = second,
                        phaseProgress = (durationSeconds - second).toFloat() / durationSeconds.toFloat(),
                        currentCycle = cycle,
                        targetCycles = targetCycles
                    )
                )
            }
            delay(1000)
        }
    }

    private fun stopBreathingTimer() {
        breathingJob?.cancel()
        breathingJob = null
        _state.update {
            it.copy(
                breathingState = it.breathingState.copy(
                    isActive = false,
                    isPaused = false,
                    isCompleted = false
                )
            )
        }
    }

    override fun onDispose() {
        stopBreathingTimer()
        super.onDispose()
    }
}
