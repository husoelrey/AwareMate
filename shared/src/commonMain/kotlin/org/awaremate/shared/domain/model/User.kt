package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val createdAtEpochMs: Long = 0L,
    val lastActiveEpochMs: Long = 0L
)

@Serializable
data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val nudgeThresholdMinutes: Int = 30,
    val dailyScreenTimeGoalMinutes: Int = 180,
    val bedtimeHour: Int = 22,
    val bedtimeMinute: Int = 30,
    val missedCheckInReminderEnabled: Boolean = true,
    val missedCheckInReminderHour: Int = 18,
    val missedCheckInReminderMinute: Int = 0,
    val lastMissedCheckInNotificationDate: String? = null,
    val dynamicColorEnabled: Boolean = true,
    val themeMode: String = "SYSTEM"
)
