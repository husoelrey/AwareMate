package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.test.FakeDailyChallengeDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DailyChallengeRepositoryTest {

    @Test
    fun testSaveAndCompleteDailyChallenge() = runTest {
        val dao = FakeDailyChallengeDao()
        val repo = DailyChallengeRepositoryImpl(dao)

        val challenge = DailyChallenge(
            id = "dc-1",
            userId = "u-1",
            title = "Mindful Breathing",
            description = "Complete 3 min breathing session",
            category = CompanionCategory.WISDOM,
            xpReward = 25,
            dateString = "2026-09-02",
            completed = false
        )

        repo.saveChallenges(listOf(challenge)).getOrThrow()

        val list = repo.getChallengesForDate("2026-09-02").first()
        assertEquals(1, list.size)
        assertEquals("dc-1", list[0].id)

        repo.completeChallenge("dc-1").getOrThrow()

        val updatedList = repo.getChallengesForDate("2026-09-02").first()
        assertTrue(updatedList[0].completed)
    }
}
