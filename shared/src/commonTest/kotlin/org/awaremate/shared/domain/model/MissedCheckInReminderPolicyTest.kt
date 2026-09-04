package org.awaremate.shared.domain.model

import kotlinx.datetime.LocalDateTime
import org.awaremate.shared.domain.usecase.sunset.SunsetStage
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MissedCheckInReminderPolicyTest {
    private val evening = LocalDateTime(2026, 9, 4, 18, 30)

    @Test
    fun eligibleOnlyAfterConfiguredTimeWithoutMood() {
        val preferences = UserPreferences(missedCheckInReminderHour = 18)
        assertTrue(MissedCheckInReminderPolicy.shouldNotify(preferences, evening, false, SunsetStage.DAYTIME))
        assertFalse(
            MissedCheckInReminderPolicy.shouldNotify(
                preferences,
                LocalDateTime(2026, 9, 4, 17, 59),
                false,
                SunsetStage.DAYTIME
            )
        )
        assertFalse(MissedCheckInReminderPolicy.shouldNotify(preferences, evening, true, SunsetStage.DAYTIME))
    }

    @Test
    fun toggleAndGlobalNotificationSettingAreRespected() {
        assertFalse(
            MissedCheckInReminderPolicy.shouldNotify(
                UserPreferences(missedCheckInReminderEnabled = false), evening, false, SunsetStage.DAYTIME
            )
        )
        assertFalse(
            MissedCheckInReminderPolicy.shouldNotify(
                UserPreferences(notificationsEnabled = false), evening, false, SunsetStage.DAYTIME
            )
        )
    }

    @Test
    fun neverNotifiesTwiceOnSameLocalDate() {
        val preferences = UserPreferences(lastMissedCheckInNotificationDate = "2026-09-04")
        assertFalse(MissedCheckInReminderPolicy.shouldNotify(preferences, evening, false, SunsetStage.DAYTIME))
    }

    @Test
    fun digitalSunsetAndBedtimeSuppressInvitation() {
        val preferences = UserPreferences()
        assertFalse(MissedCheckInReminderPolicy.shouldNotify(preferences, evening, false, SunsetStage.SUNSET_ACTIVE))
        assertFalse(MissedCheckInReminderPolicy.shouldNotify(preferences, evening, false, SunsetStage.BEDTIME))
    }

    @Test
    fun messageIsFixedAndContainsNoEscalatingOrGuiltLanguage() {
        val body = MindfulNudgeCatalog.missedCheckIn.body.lowercase()
        listOf("don't forget", "falling behind", "missed", "days in a row", "must", "should").forEach {
            assertFalse(body.contains(it))
        }
    }
}
