package org.awaremate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CompanionGrowthRulesTest {

    @Test
    fun testStageThresholdsExactBoundaries() {
        // SEED: 0 .. 99
        assertEquals(CompanionStage.SEED, CompanionGrowthRules.getStageForXp(0))
        assertEquals(CompanionStage.SEED, CompanionGrowthRules.getStageForXp(50))
        assertEquals(CompanionStage.SEED, CompanionGrowthRules.getStageForXp(99))

        // SPROUT: 100 .. 299
        assertEquals(CompanionStage.SPROUT, CompanionGrowthRules.getStageForXp(100))
        assertEquals(CompanionStage.SPROUT, CompanionGrowthRules.getStageForXp(150))
        assertEquals(CompanionStage.SPROUT, CompanionGrowthRules.getStageForXp(299))

        // BLOOM: 300 .. 599
        assertEquals(CompanionStage.BLOOM, CompanionGrowthRules.getStageForXp(300))
        assertEquals(CompanionStage.BLOOM, CompanionGrowthRules.getStageForXp(450))
        assertEquals(CompanionStage.BLOOM, CompanionGrowthRules.getStageForXp(599))

        // TREE: 600 .. 999
        assertEquals(CompanionStage.TREE, CompanionGrowthRules.getStageForXp(600))
        assertEquals(CompanionStage.TREE, CompanionGrowthRules.getStageForXp(800))
        assertEquals(CompanionStage.TREE, CompanionGrowthRules.getStageForXp(999))

        // ANCIENT_TREE: 1000+
        assertEquals(CompanionStage.ANCIENT_TREE, CompanionGrowthRules.getStageForXp(1000))
        assertEquals(CompanionStage.ANCIENT_TREE, CompanionGrowthRules.getStageForXp(1500))
        assertEquals(CompanionStage.ANCIENT_TREE, CompanionGrowthRules.getStageForXp(10000))
    }

    @Test
    fun testExactThresholdOneBelowAndOneAbove() {
        // SEED -> SPROUT threshold (100 XP)
        assertEquals(CompanionStage.SEED, CompanionGrowthRules.getStageForXp(99), "Threshold - 1 must be SEED")
        assertEquals(CompanionStage.SPROUT, CompanionGrowthRules.getStageForXp(100), "Exact threshold must be SPROUT")
        assertEquals(CompanionStage.SPROUT, CompanionGrowthRules.getStageForXp(101), "Threshold + 1 must be SPROUT")
        assertTrue(CompanionGrowthRules.isStageEvolution(99, 100))
        assertFalse(CompanionGrowthRules.isStageEvolution(100, 101))

        // SPROUT -> BLOOM threshold (300 XP)
        assertEquals(CompanionStage.SPROUT, CompanionGrowthRules.getStageForXp(299), "Threshold - 1 must be SPROUT")
        assertEquals(CompanionStage.BLOOM, CompanionGrowthRules.getStageForXp(300), "Exact threshold must be BLOOM")
        assertEquals(CompanionStage.BLOOM, CompanionGrowthRules.getStageForXp(301), "Threshold + 1 must be BLOOM")
        assertTrue(CompanionGrowthRules.isStageEvolution(299, 300))
        assertFalse(CompanionGrowthRules.isStageEvolution(300, 301))

        // BLOOM -> TREE threshold (600 XP)
        assertEquals(CompanionStage.BLOOM, CompanionGrowthRules.getStageForXp(599), "Threshold - 1 must be BLOOM")
        assertEquals(CompanionStage.TREE, CompanionGrowthRules.getStageForXp(600), "Exact threshold must be TREE")
        assertEquals(CompanionStage.TREE, CompanionGrowthRules.getStageForXp(601), "Threshold + 1 must be TREE")
        assertTrue(CompanionGrowthRules.isStageEvolution(599, 600))
        assertFalse(CompanionGrowthRules.isStageEvolution(600, 601))

        // TREE -> ANCIENT_TREE threshold (1000 XP)
        assertEquals(CompanionStage.TREE, CompanionGrowthRules.getStageForXp(999), "Threshold - 1 must be TREE")
        assertEquals(CompanionStage.ANCIENT_TREE, CompanionGrowthRules.getStageForXp(1000), "Exact threshold must be ANCIENT_TREE")
        assertEquals(CompanionStage.ANCIENT_TREE, CompanionGrowthRules.getStageForXp(1001), "Threshold + 1 must be ANCIENT_TREE")
        assertTrue(CompanionGrowthRules.isStageEvolution(999, 1000))
        assertFalse(CompanionGrowthRules.isStageEvolution(1000, 1001))
    }

    @Test
    fun testStageSequencingAndNextStages() {
        assertEquals(CompanionStage.SPROUT, CompanionGrowthRules.getNextStage(CompanionStage.SEED))
        assertEquals(CompanionStage.BLOOM, CompanionGrowthRules.getNextStage(CompanionStage.SPROUT))
        assertEquals(CompanionStage.TREE, CompanionGrowthRules.getNextStage(CompanionStage.BLOOM))
        assertEquals(CompanionStage.ANCIENT_TREE, CompanionGrowthRules.getNextStage(CompanionStage.TREE))
        assertNull(CompanionGrowthRules.getNextStage(CompanionStage.ANCIENT_TREE))
    }

    @Test
    fun testXpRequiredForNextStage() {
        assertEquals(100, CompanionGrowthRules.getXpRequiredForNextStage(CompanionStage.SEED))
        assertEquals(300, CompanionGrowthRules.getXpRequiredForNextStage(CompanionStage.SPROUT))
        assertEquals(600, CompanionGrowthRules.getXpRequiredForNextStage(CompanionStage.BLOOM))
        assertEquals(1000, CompanionGrowthRules.getXpRequiredForNextStage(CompanionStage.TREE))
        assertNull(CompanionGrowthRules.getXpRequiredForNextStage(CompanionStage.ANCIENT_TREE))
    }

    @Test
    fun testProgressWithinStage() {
        // SEED: 0 .. 100
        assertEquals(0.0f, CompanionGrowthRules.getProgressWithinStage(0))
        assertEquals(0.5f, CompanionGrowthRules.getProgressWithinStage(50))
        assertEquals(0.99f, CompanionGrowthRules.getProgressWithinStage(99), 0.01f)

        // SPROUT: 100 .. 300 (span = 200)
        assertEquals(0.0f, CompanionGrowthRules.getProgressWithinStage(100))
        assertEquals(0.5f, CompanionGrowthRules.getProgressWithinStage(200))

        // BLOOM: 300 .. 600 (span = 300)
        assertEquals(0.0f, CompanionGrowthRules.getProgressWithinStage(300))
        assertEquals(0.5f, CompanionGrowthRules.getProgressWithinStage(450))

        // ANCIENT_TREE: maxed out
        assertEquals(1.0f, CompanionGrowthRules.getProgressWithinStage(1000))
        assertEquals(1.0f, CompanionGrowthRules.getProgressWithinStage(5000))
    }

    @Test
    fun testRemainingXpForNextStage() {
        assertEquals(100, CompanionGrowthRules.getRemainingXpForNextStage(0))
        assertEquals(50, CompanionGrowthRules.getRemainingXpForNextStage(50))
        assertEquals(1, CompanionGrowthRules.getRemainingXpForNextStage(99))
        assertEquals(200, CompanionGrowthRules.getRemainingXpForNextStage(100))
        assertEquals(0, CompanionGrowthRules.getRemainingXpForNextStage(1000))
        assertEquals(0, CompanionGrowthRules.getRemainingXpForNextStage(2500))
    }

    @Test
    fun testIsStageEvolution() {
        // Crossing threshold
        assertTrue(CompanionGrowthRules.isStageEvolution(90, 100))
        assertTrue(CompanionGrowthRules.isStageEvolution(290, 310))
        assertTrue(CompanionGrowthRules.isStageEvolution(550, 600))
        assertTrue(CompanionGrowthRules.isStageEvolution(990, 1050))

        // Within same stage
        assertFalse(CompanionGrowthRules.isStageEvolution(10, 50))
        assertFalse(CompanionGrowthRules.isStageEvolution(100, 250))
        assertFalse(CompanionGrowthRules.isStageEvolution(1000, 1500))
    }
}
