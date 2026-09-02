package org.awaremate.shared.domain.usecase.companion

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.test.FakeCompanionDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddExperienceUseCaseTest {

    @Test
    fun testAddExperienceNormalProgression() = runTest {
        val dao = FakeCompanionDao()
        val repo = CompanionRepositoryImpl(dao)
        val useCase = AddExperienceUseCase(repo)

        val result = useCase(CompanionCategory.WISDOM, 50).getOrThrow()

        assertEquals(50, result.earnedXp)
        assertEquals(CompanionCategory.WISDOM, result.category)
        assertFalse(result.isEvolution)
        assertEquals(50, result.updatedCompanion.wisdomXp)
        assertEquals(50, result.updatedCompanion.experiencePoints)
        assertEquals(CompanionStage.SEED, result.updatedCompanion.stage)
        assertEquals(CompanionEmotion.CHEERFUL, result.updatedCompanion.emotion)

        val saved = repo.getCompanion().first()
        assertEquals(50, saved?.experiencePoints)
    }

    @Test
    fun testAddExperienceTriggersEvolution() = runTest {
        val dao = FakeCompanionDao()
        val repo = CompanionRepositoryImpl(dao)
        val useCase = AddExperienceUseCase(repo)

        // Adding 150 XP crosses SEED (0-99) -> SPROUT (100) threshold
        val result = useCase(CompanionCategory.HAPPINESS, 150).getOrThrow()

        assertTrue(result.isEvolution)
        assertEquals(CompanionStage.SPROUT, result.updatedCompanion.stage)
        assertEquals(150, result.updatedCompanion.happinessXp)
        assertEquals(CompanionEmotion.CHEERFUL, result.updatedCompanion.emotion)
    }

    @Test
    fun testAddNegativeExperienceThrowsException() = runTest {
        val dao = FakeCompanionDao()
        val repo = CompanionRepositoryImpl(dao)
        val useCase = AddExperienceUseCase(repo)

        assertFailsWith<IllegalArgumentException> {
            useCase(CompanionCategory.ENERGY, -10).getOrThrow()
        }
    }
}
