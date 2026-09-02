package org.awaremate.shared.presentation.onboarding

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.domain.usecase.companion.SaveCompanionUseCase

class OnboardingScreenModel(
    private val preferencesRepository: PreferencesRepository,
    private val saveCompanionUseCase: SaveCompanionUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.NextStep -> {
                val next = when (_state.value.currentStep) {
                    OnboardingStep.WELCOME -> OnboardingStep.INTERESTS
                    OnboardingStep.INTERESTS -> OnboardingStep.COMPANION_NAMING
                    OnboardingStep.COMPANION_NAMING -> OnboardingStep.PERMISSIONS
                    OnboardingStep.PERMISSIONS -> OnboardingStep.INTENTIONS
                    OnboardingStep.INTENTIONS -> OnboardingStep.INTENTIONS
                }
                _state.update { it.copy(currentStep = next) }
            }

            OnboardingIntent.PreviousStep -> {
                val prev = when (_state.value.currentStep) {
                    OnboardingStep.WELCOME -> OnboardingStep.WELCOME
                    OnboardingStep.INTERESTS -> OnboardingStep.WELCOME
                    OnboardingStep.COMPANION_NAMING -> OnboardingStep.INTERESTS
                    OnboardingStep.PERMISSIONS -> OnboardingStep.COMPANION_NAMING
                    OnboardingStep.INTENTIONS -> OnboardingStep.PERMISSIONS
                }
                _state.update { it.copy(currentStep = prev) }
            }

            is OnboardingIntent.ToggleInterest -> {
                val current = _state.value.selectedInterests
                val updated = if (current.contains(intent.interest)) {
                    if (current.size > 1) current - intent.interest else current
                } else {
                    current + intent.interest
                }
                _state.update { it.copy(selectedInterests = updated) }
            }

            is OnboardingIntent.SetCompanionName -> {
                _state.update { it.copy(companionName = intent.name) }
            }

            is OnboardingIntent.SetNotificationsEnabled -> {
                _state.update { it.copy(notificationsEnabled = intent.enabled) }
            }

            is OnboardingIntent.SetScreenTimeGoal -> {
                _state.update { it.copy(dailyScreenTimeGoalMinutes = intent.minutes) }
            }

            is OnboardingIntent.SetNudgeThreshold -> {
                _state.update { it.copy(nudgeThresholdMinutes = intent.minutes) }
            }

            is OnboardingIntent.SetBedtime -> {
                _state.update { it.copy(bedtimeHour = intent.hour, bedtimeMinute = intent.minute) }
            }

            OnboardingIntent.FinishOnboarding -> {
                completeOnboarding()
            }
        }
    }

    private fun completeOnboarding() {
        val currentState = _state.value
        _state.update { it.copy(isLoading = true) }

        screenModelScope.launch {
            val initialCompanion = Companion(
                id = "primary",
                name = currentState.companionName.ifBlank { "Sprout" },
                stage = CompanionStage.SEED,
                emotion = CompanionEmotion.PEACEFUL,
                experiencePoints = 0,
                momentumScore = 100.0,
                lastUpdatedEpochMs = 0L
            )

            val companionResult = saveCompanionUseCase(initialCompanion)
            if (companionResult.isFailure) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = companionResult.exceptionOrNull()?.message ?: "Failed to save companion"
                    )
                }
                return@launch
            }

            val prefResult = preferencesRepository.updatePreferences { prefs ->
                prefs.copy(
                    onboardingCompleted = true,
                    notificationsEnabled = currentState.notificationsEnabled,
                    dailyScreenTimeGoalMinutes = currentState.dailyScreenTimeGoalMinutes,
                    nudgeThresholdMinutes = currentState.nudgeThresholdMinutes,
                    bedtimeHour = currentState.bedtimeHour,
                    bedtimeMinute = currentState.bedtimeMinute
                )
            }

            if (prefResult.isSuccess) {
                _state.update { it.copy(isLoading = false, isCompleted = true) }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = prefResult.exceptionOrNull()?.message ?: "Failed to save preferences"
                    )
                }
            }
        }
    }
}
