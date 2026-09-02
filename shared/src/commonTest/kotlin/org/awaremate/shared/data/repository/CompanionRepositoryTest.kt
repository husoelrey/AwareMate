package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.test.FakeCloudSyncService
import org.awaremate.shared.test.FakeCompanionDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompanionRepositoryTest {

    @Test
    fun testInitialCompanionDefaults() = runTest {
        val dao = FakeCompanionDao()
        val sync = FakeCloudSyncService()
        val repo = CompanionRepositoryImpl(dao, sync)

        val companion = repo.getCompanion().first()
        assertEquals("primary", companion?.id)
        assertEquals(CompanionStage.SEED, companion?.stage)
        assertEquals(0, companion?.experiencePoints)
    }

    @Test
    fun testAddExperienceAndStageProgression() = runTest {
        val dao = FakeCompanionDao()
        val sync = FakeCloudSyncService()
        val repo = CompanionRepositoryImpl(dao, sync)

        // Add 120 Happiness XP -> total 120 -> SPROUT
        val res1 = repo.addExperience(CompanionCategory.HAPPINESS, 120).getOrThrow()
        assertEquals(120, res1.experiencePoints)
        assertEquals(120, res1.happinessXp)
        assertEquals(CompanionStage.SPROUT, res1.stage)
        assertTrue(sync.backedUpCompanions.isNotEmpty())

        // Add 200 Energy XP -> total 320 -> BLOOM
        val res2 = repo.addExperience(CompanionCategory.ENERGY, 200).getOrThrow()
        assertEquals(320, res2.experiencePoints)
        assertEquals(200, res2.energyXp)
        assertEquals(CompanionStage.BLOOM, res2.stage)

        // Add 300 Wisdom XP -> total 620 -> TREE
        val res3 = repo.addExperience(CompanionCategory.WISDOM, 300).getOrThrow()
        assertEquals(620, res3.experiencePoints)
        assertEquals(CompanionStage.TREE, res3.stage)

        // Add 400 Creativity XP -> total 1020 -> ANCIENT_TREE
        val res4 = repo.addExperience(CompanionCategory.CREATIVITY, 400).getOrThrow()
        assertEquals(1020, res4.experiencePoints)
        assertEquals(CompanionStage.ANCIENT_TREE, res4.stage)
    }

    @Test
    fun testUpdateMomentum() = runTest {
        val dao = FakeCompanionDao()
        val sync = FakeCloudSyncService()
        val repo = CompanionRepositoryImpl(dao, sync)

        repo.updateMomentum(85.5).getOrThrow()
        val companion = repo.getCompanion().first()
        assertEquals(85.5, companion?.momentumScore)
    }
}
