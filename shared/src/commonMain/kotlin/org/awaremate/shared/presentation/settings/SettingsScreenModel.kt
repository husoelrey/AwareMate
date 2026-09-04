package org.awaremate.shared.presentation.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.domain.service.MissedCheckInReminderScheduler
import org.awaremate.shared.domain.usecase.account.DeleteAccountResult
import org.awaremate.shared.domain.usecase.account.DeleteAccountUseCase

class SettingsScreenModel(
    private val preferencesRepository: PreferencesRepository,
    private val missedCheckInReminderScheduler: MissedCheckInReminderScheduler? = null,
    private val deleteAccountUseCase: DeleteAccountUseCase? = null
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
                    missedCheckInReminderScheduler?.refresh()
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

            is SettingsIntent.SetMissedCheckInReminder -> {
                screenModelScope.launch {
                    preferencesRepository.updatePreferences {
                        it.copy(missedCheckInReminderEnabled = intent.enabled)
                    }
                    missedCheckInReminderScheduler?.refresh()
                }
            }

            is SettingsIntent.SetMissedCheckInTime -> {
                screenModelScope.launch {
                    preferencesRepository.updatePreferences {
                        it.copy(
                            missedCheckInReminderHour = intent.hour,
                            missedCheckInReminderMinute = intent.minute
                        )
                    }
                    missedCheckInReminderScheduler?.refresh()
                }
            }

            SettingsIntent.DeleteAccount -> deleteAccount()

            SettingsIntent.ClearAccountDeletionError -> {
                _state.update { it.copy(accountDeletionError = null) }
            }

            SettingsIntent.ClearInfoMessage -> {
                _state.update { it.copy(infoMessage = null) }
            }
        }
    }

    private fun deleteAccount() {
        if (_state.value.isDeletingAccount) return
        val useCase = deleteAccountUseCase ?: run {
            _state.update { it.copy(accountDeletionError = "Account deletion is unavailable right now.") }
            return
        }

        screenModelScope.launch {
            _state.update {
                it.copy(isDeletingAccount = true, accountDeletionError = null)
            }
            when (val result = useCase()) {
                DeleteAccountResult.Success -> _state.update {
                    it.copy(isDeletingAccount = false, accountDeletionCompleted = true)
                }

                DeleteAccountResult.Offline -> showDeletionError(
                    "You're offline. Nothing was deleted. Reconnect and try again when you're ready."
                )

                DeleteAccountResult.RecentAuthenticationRequired -> showDeletionError(
                    "For your safety, please sign in again before deleting your account. Nothing on this device was deleted."
                )

                is DeleteAccountResult.Failed -> showDeletionError(result.message)
            }
        }
    }

    private fun showDeletionError(message: String) {
        _state.update {
            it.copy(isDeletingAccount = false, accountDeletionError = message)
        }
    }
}
