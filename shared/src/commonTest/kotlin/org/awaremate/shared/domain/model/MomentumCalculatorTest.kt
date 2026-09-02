package org.awaremate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MomentumCalculatorTest {

    @Test
    fun testGradualDecayFormulaCompassionateNonPunitive() {
        val initial = 100.0

        // 0 days inactive -> score intact
        val day0 = MomentumCalculator.calculateDecayedScore(initial, 0)
        assertEquals(100.0, day0, 0.001)

        // 1 day missed -> 90.0 (10% soft decay, NEVER 0)
        val day1 = MomentumCalculator.calculateDecayedScore(initial, 1)
        assertEquals(90.0, day1, 0.001)
        assertTrue(day1 > 0.0, "Missing one day must never reset momentum to zero")

        // 2 days missed -> 81.0
        val day2 = MomentumCalculator.calculateDecayedScore(initial, 2)
        assertEquals(81.0, day2, 0.001)

        // 3 days missed -> 72.9
        val day3 = MomentumCalculator.calculateDecayedScore(initial, 3)
        assertEquals(72.9, day3, 0.001)

        // 7 days (1 full week) missed -> ~47.8
        val day7 = MomentumCalculator.calculateDecayedScore(initial, 7)
        assertEquals(47.829, day7, 0.01)

        // 14 days (2 full weeks) missed -> ~22.87
        val day14 = MomentumCalculator.calculateDecayedScore(initial, 14)
        assertEquals(22.876, day14, 0.01)

        // 30 days (1 full month) missed -> ~4.23 (still above 0)
        val day30 = MomentumCalculator.calculateDecayedScore(initial, 30)
        assertEquals(4.239, day30, 0.01)
        assertTrue(day30 >= 0.0)
    }

    @Test
    fun testScoreClampingLimits() {
        // Upper bound clamp
        val overMax = MomentumCalculator.calculateDecayedScore(150.0, 0)
        assertEquals(100.0, overMax)

        // Lower bound clamp
        val underMin = MomentumCalculator.calculateDecayedScore(-20.0, 0)
        assertEquals(0.0, underMin)

        // Negative daysInactive treated safely as 0
        val negativeDays = MomentumCalculator.calculateDecayedScore(80.0, -3)
        assertEquals(80.0, negativeDays)
    }

    @Test
    fun testActivityBoostRegularAndComeback() {
        // Regular activity gain (0-1 days inactive)
        val regularBoost = MomentumCalculator.calculateBoostedScore(
            currentScore = 50.0,
            baseBoost = 10.0,
            daysInactive = 1
        )
        assertEquals(60.0, regularBoost)

        // Comeback bonus (>= 2 days inactive -> 1.5x multiplier)
        val comebackBoost = MomentumCalculator.calculateBoostedScore(
            currentScore = 50.0,
            baseBoost = 10.0,
            daysInactive = 3
        )
        // 50 + (10 * 1.5) = 65.0
        assertEquals(65.0, comebackBoost)

        // Max clamp at 100.0
        val maxedBoost = MomentumCalculator.calculateBoostedScore(
            currentScore = 95.0,
            baseBoost = 15.0,
            daysInactive = 0
        )
        assertEquals(100.0, maxedBoost)
    }

    @Test
    fun testMomentumTiers() {
        assertEquals(MomentumTier.SPARKING, MomentumCalculator.getTierForScore(100.0))
        assertEquals(MomentumTier.SPARKING, MomentumCalculator.getTierForScore(80.0))
        assertEquals(MomentumTier.FLOWING, MomentumCalculator.getTierForScore(79.9))
        assertEquals(MomentumTier.FLOWING, MomentumCalculator.getTierForScore(50.0))
        assertEquals(MomentumTier.STEADY, MomentumCalculator.getTierForScore(49.9))
        assertEquals(MomentumTier.STEADY, MomentumCalculator.getTierForScore(25.0))
        assertEquals(MomentumTier.RESTING, MomentumCalculator.getTierForScore(24.9))
        assertEquals(MomentumTier.RESTING, MomentumCalculator.getTierForScore(0.0))
    }
}
