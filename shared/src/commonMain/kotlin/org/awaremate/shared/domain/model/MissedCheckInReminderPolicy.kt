package org.awaremate.shared.domain.model

import kotlinx.datetime.LocalDateTime
import org.awaremate.shared.domain.usecase.sunset.SunsetStage

object MissedCheckInReminderPolicy {
    fun shouldNotify(
        preferences: UserPreferences,
        currentLocalDateTime: LocalDateTime,
        hasMoodEntryToday: Boolean,
        sunsetStage: SunsetStage
    ): Boolean {
        if (!preferences.notificationsEnabled || !preferences.missedCheckInReminderEnabled) return false
        if (hasMoodEntryToday) return false
        if (preferences.lastMissedCheckInNotificationDate == currentLocalDateTime.date.toString()) return false

        val currentMinutes = currentLocalDateTime.hour * 60 + currentLocalDateTime.minute
        val reminderMinutes = preferences.missedCheckInReminderHour * 60 + preferences.missedCheckInReminderMinute
        if (currentMinutes < reminderMinutes) return false

        return sunsetStage != SunsetStage.SUNSET_ACTIVE && sunsetStage != SunsetStage.BEDTIME
    }
}
