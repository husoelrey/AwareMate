package org.awaremate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CompanionTest {

    @Test
    fun testCompanionDefaults() {
        val companion = Companion()
        assertEquals("primary", companion.id)
        assertEquals("Sprout", companion.name)
        assertEquals(CompanionStage.SEED, companion.stage)
        assertEquals(CompanionEmotion.PEACEFUL, companion.emotion)
        assertEquals(0, companion.experiencePoints)
        assertEquals(100.0, companion.momentumScore)
    }

    @Test
    fun testCompanionCategoryXp() {
        val companion = Companion(
            happinessXp = 50,
            energyXp = 30,
            wisdomXp = 40,
            creativityXp = 80,
            experiencePoints = 200,
            stage = CompanionStage.SPROUT
        )
        assertEquals(200, companion.experiencePoints)
        assertEquals(CompanionStage.SPROUT, companion.stage)
        assertEquals(50, companion.happinessXp)
        assertEquals(30, companion.energyXp)
        assertEquals(40, companion.wisdomXp)
        assertEquals(80, companion.creativityXp)
    }
}
