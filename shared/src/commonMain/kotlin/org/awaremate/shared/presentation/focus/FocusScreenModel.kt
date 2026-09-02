package org.awaremate.shared.presentation.focus

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.FocusCategory
import org.awaremate.shared.domain.model.FocusSession
import org.awaremate.shared.domain.repository.CompanionRepository
import org.awaremate.shared.domain.repository.FocusSessionRepository
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.CalculateGrowthStageUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase

class FocusScreenModel(
    private val focusSessionRepository: FocusSessionRepository,
    private val companionRepository: CompanionRepository,
    private val addExperienceUseCase: AddExperienceUseCase,
    private val updateMomentumUseCase: UpdateMomentumUseCase,
    private val calculateGrowthStageUseCase: CalculateGrowthStageUseCase = CalculateGrowthStageUseCase(),
    private val clock: Clock = Clock.System
) : ScreenModel {

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        screenModelScope.launch {
            companionRepository.getCompanion().collect { companion ->
                if (companion != null) {
                    val stage = calculateGrowthStageUseCase(companion.experiencePoints).stage
                    _state.update {
                        it.copy(
                            companionStage = stage,
                            companionEmotion = if (it.status == FocusTimerStatus.RUNNING) CompanionEmotion.PEACEFUL else companion.emotion
                        )
                    }
                }
            }
        }

        screenModelScope.launch {
            focusSessionRepository.getRecentSessions(20).collect { sessions ->
                val totalMinutes = focusSessionRepository.getTotalFocusMinutes()
                _state.update {
                    it.copy(
                        recentSessions = sessions,
                        totalFocusMinutesToday = totalMinutes
                    )
                }
            }
        }
    }

    fun handleIntent(intent: FocusIntent) {
        when (intent) {
            is FocusIntent.SelectDuration -> selectDuration(intent.minutes)
            is FocusIntent.SelectCategory -> selectCategory(intent.category)
            FocusIntent.StartTimer -> startTimer()
            FocusIntent.PauseTimer -> pauseTimer()
            FocusIntent.ResumeTimer -> resumeTimer()
            FocusIntent.StopTimer -> stopTimer()
            FocusIntent.DismissCelebration -> dismissCelebration()
        }
    }

    private fun selectDuration(minutes: Int) {
        if (_state.value.status == FocusTimerStatus.IDLE) {
            _state.update {
                it.copy(
                    selectedDurationMinutes = minutes,
                    remainingSeconds = minutes * 60
                )
            }
        }
    }

    private fun selectCategory(category: FocusCategory) {
        if (_state.value.status == FocusTimerStatus.IDLE) {
            _state.update { it.copy(selectedCategory = category) }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        val now = clock.now().toEpochMilliseconds()
        val targetEndTime = now + (_state.value.remainingSeconds * 1000L)
        _state.update {
            it.copy(
                status = FocusTimerStatus.RUNNING,
                companionEmotion = CompanionEmotion.PEACEFUL,
                targetEndTimeEpochMs = targetEndTime
            )
        }
        runCountdown(targetEndTime)
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        val now = clock.now().toEpochMilliseconds()
        val target = _state.value.targetEndTimeEpochMs ?: (now + _state.value.remainingSeconds * 1000L)
        val remaining = ((target - now) / 1000L).coerceAtLeast(0).toInt()
        _state.update {
            it.copy(
                status = FocusTimerStatus.PAUSED,
                companionEmotion = CompanionEmotion.CURIOUS,
                remainingSeconds = remaining,
                targetEndTimeEpochMs = null
            )
        }
    }

    private fun resumeTimer() {
        timerJob?.cancel()
        val now = clock.now().toEpochMilliseconds()
        val targetEndTime = now + (_state.value.remainingSeconds * 1000L)
        _state.update {
            it.copy(
                status = FocusTimerStatus.RUNNING,
                companionEmotion = CompanionEmotion.PEACEFUL,
                targetEndTimeEpochMs = targetEndTime
            )
        }
        runCountdown(targetEndTime)
    }

    private fun stopTimer() {
        timerJob?.cancel()
        val duration = _state.value.selectedDurationMinutes
        _state.update {
            it.copy(
                status = FocusTimerStatus.IDLE,
                remainingSeconds = duration * 60,
                companionEmotion = CompanionEmotion.PEACEFUL,
                targetEndTimeEpochMs = null
            )
        }
    }

    private fun runCountdown(targetEndTime: Long) {
        timerJob = screenModelScope.launch {
            while (true) {
                val now = clock.now().toEpochMilliseconds()
                val remaining = ((targetEndTime - now) / 1000L).coerceAtLeast(0).toInt()
                _state.update { it.copy(remainingSeconds = remaining) }
                if (remaining <= 0) {
                    onTimerComplete()
                    break
                }
                delay(1000L)
            }
        }
    }

    private suspend fun onTimerComplete() {
        val durationMinutes = _state.value.selectedDurationMinutes
        val xpEarned = durationMinutes * 2

        // Record focus session
        val session = FocusSession(
            id = "focus_${clock.now().toEpochMilliseconds()}",
            userId = "local_user",
            startTimeEpochMs = clock.now().toEpochMilliseconds() - (durationMinutes * 60 * 1000L),
            durationSeconds = durationMinutes * 60,
            category = _state.value.selectedCategory,
            earnedXp = xpEarned,
            completed = true
        )
        focusSessionRepository.saveSession(session)

        // Award companion XP and momentum
        val wisdomXp = xpEarned / 2
        val energyXp = xpEarned - wisdomXp
        if (wisdomXp > 0) {
            addExperienceUseCase(CompanionCategory.WISDOM, wisdomXp)
        }
        if (energyXp > 0) {
            addExperienceUseCase(CompanionCategory.ENERGY, energyXp)
        }
        updateMomentumUseCase.boostMomentum((durationMinutes / 5.0).coerceAtLeast(3.0))

        _state.update {
            it.copy(
                status = FocusTimerStatus.COMPLETED,
                companionEmotion = CompanionEmotion.CHEERFUL,
                remainingSeconds = 0,
                targetEndTimeEpochMs = null,
                earnedXp = xpEarned,
                showCelebrationDialog = true,
                totalFocusMinutesToday = it.totalFocusMinutesToday + durationMinutes
            )
        }
    }

    private fun dismissCelebration() {
        val duration = _state.value.selectedDurationMinutes
        _state.update {
            it.copy(
                status = FocusTimerStatus.IDLE,
                remainingSeconds = duration * 60,
                showCelebrationDialog = false,
                companionEmotion = CompanionEmotion.PEACEFUL
            )
        }
    }

    override fun onDispose() {
        timerJob?.cancel()
        super.onDispose()
    }
}
