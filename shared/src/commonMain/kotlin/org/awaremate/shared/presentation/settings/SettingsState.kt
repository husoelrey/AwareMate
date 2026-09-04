package org.awaremate.shared.presentation.settings

import org.awaremate.shared.domain.model.UserPreferences

data class SettingsState(
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val infoMessage: String? = null,
    val isDeletingAccount: Boolean = false,
    val accountDeletionError: String? = null,
    val accountDeletionCompleted: Boolean = false
)

sealed interface SettingsIntent {
    data object LoadSettings : SettingsIntent
    data class SetThemeMode(val themeMode: String) : SettingsIntent
    data class SetDynamicColor(val enabled: Boolean) : SettingsIntent
    data class SetNotifications(val enabled: Boolean) : SettingsIntent
    data class SetDailyGoal(val minutes: Int) : SettingsIntent
    data class SetNudgeThreshold(val minutes: Int) : SettingsIntent
    data class SetBedtime(val hour: Int, val minute: Int) : SettingsIntent
    data class SetMissedCheckInReminder(val enabled: Boolean) : SettingsIntent
    data class SetMissedCheckInTime(val hour: Int, val minute: Int = 0) : SettingsIntent
    data object DeleteAccount : SettingsIntent
    data object ClearAccountDeletionError : SettingsIntent
    data object ClearInfoMessage : SettingsIntent
}
