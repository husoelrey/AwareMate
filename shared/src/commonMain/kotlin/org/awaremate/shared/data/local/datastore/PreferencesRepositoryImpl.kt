package org.awaremate.shared.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import org.awaremate.shared.domain.model.UserPreferences
import org.awaremate.shared.domain.repository.PreferencesRepository

class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NUDGE_THRESHOLD_MINUTES = intPreferencesKey("nudge_threshold_minutes")
        val DAILY_SCREEN_TIME_GOAL = intPreferencesKey("daily_screen_time_goal_minutes")
        val BEDTIME_HOUR = intPreferencesKey("bedtime_hour")
        val BEDTIME_MINUTE = intPreferencesKey("bedtime_minute")
        val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    override fun getPreferences(): Flow<UserPreferences> {
        return dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs ->
                UserPreferences(
                    onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                    notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                    nudgeThresholdMinutes = prefs[Keys.NUDGE_THRESHOLD_MINUTES] ?: 30,
                    dailyScreenTimeGoalMinutes = prefs[Keys.DAILY_SCREEN_TIME_GOAL] ?: 180,
                    bedtimeHour = prefs[Keys.BEDTIME_HOUR] ?: 22,
                    bedtimeMinute = prefs[Keys.BEDTIME_MINUTE] ?: 30,
                    dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR_ENABLED] ?: true,
                    themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM"
                )
            }
    }

    override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences): Result<Unit> = runCatching {
        dataStore.edit { prefs ->
            val current = UserPreferences(
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                nudgeThresholdMinutes = prefs[Keys.NUDGE_THRESHOLD_MINUTES] ?: 30,
                dailyScreenTimeGoalMinutes = prefs[Keys.DAILY_SCREEN_TIME_GOAL] ?: 180,
                bedtimeHour = prefs[Keys.BEDTIME_HOUR] ?: 22,
                bedtimeMinute = prefs[Keys.BEDTIME_MINUTE] ?: 30,
                dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR_ENABLED] ?: true,
                themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM"
            )
            val updated = transform(current)
            prefs[Keys.ONBOARDING_COMPLETED] = updated.onboardingCompleted
            prefs[Keys.NOTIFICATIONS_ENABLED] = updated.notificationsEnabled
            prefs[Keys.NUDGE_THRESHOLD_MINUTES] = updated.nudgeThresholdMinutes
            prefs[Keys.DAILY_SCREEN_TIME_GOAL] = updated.dailyScreenTimeGoalMinutes
            prefs[Keys.BEDTIME_HOUR] = updated.bedtimeHour
            prefs[Keys.BEDTIME_MINUTE] = updated.bedtimeMinute
            prefs[Keys.DYNAMIC_COLOR_ENABLED] = updated.dynamicColorEnabled
            prefs[Keys.THEME_MODE] = updated.themeMode
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> = runCatching {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }
}

fun createDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
}
