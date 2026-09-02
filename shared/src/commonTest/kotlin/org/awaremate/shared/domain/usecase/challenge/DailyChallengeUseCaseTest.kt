package org.awaremate.shared.domain.usecase.challenge

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.data.repository.DailyChallengeRepositoryImpl
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.test.FakeCompanionDao
import org.awaremate.shared.test.FakeDailyChallengeDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DailyChallengeUseCaseTest {

    @Test
    fun testGenerateAndRetrieveDailyChallenges() = runTest {
        val challengeDao = FakeDailyChallengeDao()
        val challengeRepo = DailyChallengeRepositoryImpl(challengeDao)
        val generateUseCase = GenerateDailyChallengesUseCase(challengeRepo)
        val getUseCase = GetDailyChallengesUseCase(challengeRepo)

        val dateString = "2026-09-02"
        val generated = generateUseCase(dateString).getOrThrow()
        assertEquals(3, generated.size)

        val observed = getUseCase(dateString).first()
        assertEquals(3, observed.size)
        assertEquals(generated.map { it.id }, observed.map { it.id })
    }

    @Test
    fun testGetOrGenerateChallengesAutoGeneratesWhenEmpty() = runTest {
        val challengeDao = FakeDailyChallengeDao()
        val challengeRepo = DailyChallengeRepositoryImpl(challengeDao)
        val getUseCase = GetDailyChallengesUseCase(challengeRepo)

        val dateString = "2026-09-05"
        val challenges = getUseCase.getOrGenerateChallenges(dateString).getOrThrow()

        assertEquals(3, challenges.size)
        val inDao = challengeDao.getChallengesForDateFlow(dateString).first()
        assertEquals(3, inDao.size)
    }

    @Test
    fun testCompleteDailyChallengeAwardsXpAndBoostsMomentum() = runTest {
        val challengeDao = FakeDailyChallengeDao()
        val challengeRepo = DailyChallengeRepositoryImpl(challengeDao)
        val companionDao = FakeCompanionDao()
        val companionRepo = CompanionRepositoryImpl(companionDao)

        val addExpUseCase = AddExperienceUseCase(companionRepo)
        val completeChallengeUseCase = CompleteDailyChallengeUseCase(challengeRepo, addExpUseCase)
        val generateUseCase = GenerateDailyChallengesUseCase(challengeRepo)

        val challenges = generateUseCase("2026-09-02").getOrThrow()
        val targetChallenge = challenges.first()

        val result = completeChallengeUseCase(targetChallenge).getOrThrow()

        // Verify challenge completion in DB
        val updatedChallenge = challengeRepo.getChallengesForDate("2026-09-02").first().first { it.id == targetChallenge.id }
        assertTrue(updatedChallenge.completed)
        assertTrue(updatedChallenge.completedAtEpochMs != null)

        // Verify companion received XP in target category
        val companion = companionRepo.getCompanion().first()
        assertEquals(targetChallenge.xpReward, companion?.experiencePoints)
        assertEquals(CompanionEmotion.CHEERFUL, companion?.emotion)
        assertEquals(targetChallenge.xpReward, result.earnedXp)
    }
}
