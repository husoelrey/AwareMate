package org.awaremate.shared.domain.usecase.companion

import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionStage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalculateGrowthStageUseCaseTest {

    private val useCase = CalculateGrowthStageUseCase()

    @Test
    fun testSeedMetrics() {
        val metrics = useCase(50)
        assertEquals(CompanionStage.SEED, metrics.stage)
        assertEquals(50, metrics.totalXp)
        assertEquals(0.5f, metrics.progressWithinStage)
        assertEquals(50, metrics.remainingXpForNextStage)
        assertEquals(CompanionStage.SPROUT, metrics.nextStage)
        assertFalse(metrics.isMaxStage)
    }

    @Test
    fun testSproutMetrics() {
        val metrics = useCase(200)
        assertEquals(CompanionStage.SPROUT, metrics.stage)
        assertEquals(200, metrics.totalXp)
        assertEquals(0.5f, metrics.progressWithinStage)
        assertEquals(100, metrics.remainingXpForNextStage)
        assertEquals(CompanionStage.BLOOM, metrics.nextStage)
        assertFalse(metrics.isMaxStage)
    }

    @Test
    fun testBloomMetrics() {
        val metrics = useCase(450)
        assertEquals(CompanionStage.BLOOM, metrics.stage)
        assertEquals(450, metrics.totalXp)
        assertEquals(0.5f, metrics.progressWithinStage)
        assertEquals(150, metrics.remainingXpForNextStage)
        assertEquals(CompanionStage.TREE, metrics.nextStage)
        assertFalse(metrics.isMaxStage)
    }

    @Test
    fun testTreeMetrics() {
        val metrics = useCase(800)
        assertEquals(CompanionStage.TREE, metrics.stage)
        assertEquals(800, metrics.totalXp)
        assertEquals(0.5f, metrics.progressWithinStage)
        assertEquals(200, metrics.remainingXpForNextStage)
        assertEquals(CompanionStage.ANCIENT_TREE, metrics.nextStage)
        assertFalse(metrics.isMaxStage)
    }

    @Test
    fun testAncientTreeMetrics() {
        val companion = Companion(experiencePoints = 1200)
        val metrics = useCase(companion)
        assertEquals(CompanionStage.ANCIENT_TREE, metrics.stage)
        assertEquals(1200, metrics.totalXp)
        assertEquals(1.0f, metrics.progressWithinStage)
        assertEquals(0, metrics.remainingXpForNextStage)
        assertNull(metrics.nextStage)
        assertTrue(metrics.isMaxStage)
    }
}
