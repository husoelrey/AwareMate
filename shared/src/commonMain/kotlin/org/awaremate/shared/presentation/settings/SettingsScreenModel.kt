package org.awaremate.shared.presentation.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.awaremate.shared.domain.repository.PreferencesRepository

class SettingsScreenModel(
    private val preferencesRepository: PreferencesRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SettingsState(isLoading = true))
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        screenModelScope.launch {
            preferencesRepository.getPreferences().collect { prefs ->
                _state.update { it.copy(preferences = prefs, isLoading = false) }
            }
        }
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.LoadSettings -> observePreferences()

            is SettingsIntent.SetThemeMode -> {
                screenModelScope.launch {
                    preferencesRepository.updatePreferences { it.copy(themeMode = intent.themeMode) }
                }
            }

            is SettingsIntent.SetDynamicColor -> {
                screenModelScope.launch {
                    preferencesRepository.updatePreferences { it.copy(dynamicColorEnabled = intent.enabled) }
                }
            }

            is SettingsIntent.SetNotifications -> {
                screenModelScope.launch {
                    preferencesRepository.updatePreferences { it.copy(notificationsEnabled = intent.enabled) }
                }
            }

            is SettingsIntent.SetDailyGoal -> {
                screenModelScope.launch {
                    preferencesRepository.updatePreferences { it.copy(dailyScreenTimeGoalMinutes = intent.minutes) }
                }
            }

            is SettingsIntent.SetNudgeThreshold -> {
                screenModelScope.launch {
                    preferencesRepository.updatePreferences { it.copy(nudgeThresholdMinutes = intent.minutes) }
                }
            }

            is SettingsIntent.SetBedtime -> {
                screenModelScope.launch {
                    preferencesRepository.updatePreferences { it.copy(bedtimeHour = intent.hour, bedtimeMinute = intent.minute) }
                }
            }

            SettingsIntent.ClearInfoMessage -> {
                _state.update { it.copy(infoMessage = null) }
            }
        }
    }
}
