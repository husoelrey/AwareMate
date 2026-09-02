package org.awaremate.shared.domain.usecase.sunset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DigitalSunsetUseCaseTest {

    private val useCase = DigitalSunsetUseCase()

    @Test
    fun testDaytimeStatusCalculation() {
        val status = useCase.calculate(
            currentHour = 14,
            currentMinute = 30,
            bedtimeHour = 22,
            bedtimeMinute = 30
        )

        assertEquals(SunsetStage.DAYTIME, status.stage)
        assertEquals(21, status.sunsetHour)
        assertEquals(45, status.sunsetMinute)
        assertTrue(status.minutesUntilSunset > 60)
        assertTrue(status.message.isNotBlank())
    }

    @Test
    fun testSunsetApproachingWithinOneHour() {
        val status = useCase.calculate(
            currentHour = 21,
            currentMinute = 15,
            bedtimeHour = 22,
            bedtimeMinute = 30
        )

        // Sunset is at 21:45, current is 21:15 -> 30 min until sunset
        assertEquals(SunsetStage.SUNSET_APPROACHING, status.stage)
        assertEquals(30, status.minutesUntilSunset)
        assertTrue(status.message.contains("Golden hour"))
    }

    @Test
    fun testSunsetActiveWindow() {
        val status = useCase.calculate(
            currentHour = 22,
            currentMinute = 0,
            bedtimeHour = 22,
            bedtimeMinute = 30
        )

        // Sunset is at 21:45, bedtime is 22:30, current is 22:00 -> within sunset window
        assertEquals(SunsetStage.SUNSET_ACTIVE, status.stage)
        assertEquals(30, status.minutesUntilBedtime)
        assertTrue(status.message.contains("Digital Sunset is active"))
    }

    @Test
    fun testBedtimeStatus() {
        val status = useCase.calculate(
            currentHour = 23,
            currentMinute = 15,
            bedtimeHour = 22,
            bedtimeMinute = 30
        )

        assertEquals(SunsetStage.BEDTIME, status.stage)
        assertTrue(status.message.contains("Restful Night"))
    }

    @Test
    fun testDoesNotTriggerDuringDaytimeWithCustomBedtime() {
        // User sets custom bedtime to 23:00 (sunset at 22:15)
        val morningStatus = useCase.calculate(
            currentHour = 9,
            currentMinute = 0,
            bedtimeHour = 23,
            bedtimeMinute = 0
        )
        assertEquals(SunsetStage.DAYTIME, morningStatus.stage, "Must be DAYTIME in morning")

        val afternoonStatus = useCase.calculate(
            currentHour = 15,
            currentMinute = 45,
            bedtimeHour = 23,
            bedtimeMinute = 0
        )
        assertEquals(SunsetStage.DAYTIME, afternoonStatus.stage, "Must be DAYTIME in afternoon")

        val earlyEveningStatus = useCase.calculate(
            currentHour = 20,
            currentMinute = 30,
            bedtimeHour = 23,
            bedtimeMinute = 0
        )
        assertEquals(SunsetStage.DAYTIME, earlyEveningStatus.stage, "Must be DAYTIME before 60m window")
    }

    @Test
    fun testCustomEarlyBedtimeIsolation() {
        // User configures early bedtime at 21:00 (sunset at 20:15)
        val beforeApproaching = useCase.calculate(
            currentHour = 18,
            currentMinute = 0,
            bedtimeHour = 21,
            bedtimeMinute = 0
        )
        assertEquals(SunsetStage.DAYTIME, beforeApproaching.stage)

        val approaching = useCase.calculate(
            currentHour = 19,
            currentMinute = 30,
            bedtimeHour = 21,
            bedtimeMinute = 0
        )
        assertEquals(SunsetStage.SUNSET_APPROACHING, approaching.stage)

        val active = useCase.calculate(
            currentHour = 20,
            currentMinute = 30,
            bedtimeHour = 21,
            bedtimeMinute = 0
        )
        assertEquals(SunsetStage.SUNSET_ACTIVE, active.stage)

        val bedtime = useCase.calculate(
            currentHour = 21,
            currentMinute = 15,
            bedtimeHour = 21,
            bedtimeMinute = 0
        )
        assertEquals(SunsetStage.BEDTIME, bedtime.stage)
    }

    @Test
    fun testCustomLateBedtimeCrossingMidnight() {
        // User configures late bedtime at 00:30 (sunset at 23:45)
        val daytime = useCase.calculate(
            currentHour = 21,
            currentMinute = 0,
            bedtimeHour = 0,
            bedtimeMinute = 30
        )
        assertEquals(SunsetStage.DAYTIME, daytime.stage)

        val approaching = useCase.calculate(
            currentHour = 23,
            currentMinute = 0,
            bedtimeHour = 0,
            bedtimeMinute = 30
        )
        assertEquals(SunsetStage.SUNSET_APPROACHING, approaching.stage)

        val active = useCase.calculate(
            currentHour = 23,
            currentMinute = 50,
            bedtimeHour = 0,
            bedtimeMinute = 30
        )
        assertEquals(SunsetStage.SUNSET_ACTIVE, active.stage)

        val bedtime = useCase.calculate(
            currentHour = 1,
            currentMinute = 15,
            bedtimeHour = 0,
            bedtimeMinute = 30
        )
        assertEquals(SunsetStage.BEDTIME, bedtime.stage)
    }
}
