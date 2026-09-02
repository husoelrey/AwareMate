package org.awaremate.shared.domain.usecase.sunset

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.awaremate.shared.domain.repository.PreferencesRepository

enum class SunsetStage {
    DAYTIME,
    SUNSET_APPROACHING,
    SUNSET_ACTIVE,
    BEDTIME
}

data class SunsetStatus(
    val stage: SunsetStage,
    val minutesUntilSunset: Int,
    val minutesUntilBedtime: Int,
    val sunsetHour: Int,
    val sunsetMinute: Int,
    val message: String
)

class DigitalSunsetUseCase(
    private val preferencesRepository: PreferencesRepository? = null
) {
    /**
     * Pure calculation based on current hour, minute and bedtime settings.
     * Sunset begins 45 minutes prior to bedtime.
     */
    fun calculate(
        currentHour: Int,
        currentMinute: Int,
        bedtimeHour: Int,
        bedtimeMinute: Int
    ): SunsetStatus {
        val currentTotalMin = currentHour * 60 + currentMinute
        val bedtimeTotalMin = bedtimeHour * 60 + bedtimeMinute

        // Calculate sunset time (45 min before bedtime)
        var sunsetTotalMin = bedtimeTotalMin - 45
        if (sunsetTotalMin < 0) sunsetTotalMin += 24 * 60

        val sunsetHour = sunsetTotalMin / 60
        val sunsetMinute = sunsetTotalMin % 60

        val minUntilSunset = if (sunsetTotalMin >= currentTotalMin) {
            sunsetTotalMin - currentTotalMin
        } else {
            (24 * 60 - currentTotalMin) + sunsetTotalMin
        }

        val minUntilBedtime = if (bedtimeTotalMin >= currentTotalMin) {
            bedtimeTotalMin - currentTotalMin
        } else {
            (24 * 60 - currentTotalMin) + bedtimeTotalMin
        }

        val isSunsetCrossingMidnight = sunsetTotalMin > bedtimeTotalMin
        val morningWakeHourMin = 360 // 06:00 AM

        val isSunsetActive = if (!isSunsetCrossingMidnight) {
            currentTotalMin in sunsetTotalMin until bedtimeTotalMin
        } else {
            currentTotalMin >= sunsetTotalMin || currentTotalMin < bedtimeTotalMin
        }

        val isBedtime = if (!isSunsetCrossingMidnight) {
            currentTotalMin >= bedtimeTotalMin || currentTotalMin < morningWakeHourMin
        } else {
            currentTotalMin in bedtimeTotalMin until morningWakeHourMin
        }

        val stage = when {
            isSunsetActive -> SunsetStage.SUNSET_ACTIVE
            minUntilSunset in 1..60 && !isBedtime -> SunsetStage.SUNSET_APPROACHING
            isBedtime -> SunsetStage.BEDTIME
            else -> SunsetStage.DAYTIME
        }

        val message = when (stage) {
            SunsetStage.DAYTIME -> "Daytime rhythm active. Maintain mindful breaks as you journey through the day."
            SunsetStage.SUNSET_APPROACHING -> "🌅 Golden hour is arriving. Preparing to put your screens to sleep helps invite peaceful rest."
            SunsetStage.SUNSET_ACTIVE -> "🌆 Digital Sunset is active. Your screens are winding down. Time for warm tea, stretching, or reading."
            SunsetStage.BEDTIME -> "🌙 Restful Night: Thank you for caring for yourself today. Sleep gently and recharge."
        }

        return SunsetStatus(
            stage = stage,
            minutesUntilSunset = minUntilSunset,
            minutesUntilBedtime = minUntilBedtime,
            sunsetHour = sunsetHour,
            sunsetMinute = sunsetMinute,
            message = message
        )
    }

    suspend fun getStatus(epochMs: Long = Clock.System.now().toEpochMilliseconds()): SunsetStatus {
        val prefs = preferencesRepository?.getPreferences()?.first()
        val bedtimeHour = prefs?.bedtimeHour ?: 22
        val bedtimeMinute = prefs?.bedtimeMinute ?: 30

        val localTime = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())

        return calculate(
            currentHour = localTime.hour,
            currentMinute = localTime.minute,
            bedtimeHour = bedtimeHour,
            bedtimeMinute = bedtimeMinute
        )
    }
}
