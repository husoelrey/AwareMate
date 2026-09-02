package org.awaremate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChallengeCatalogTest {

    @Test
    fun testCuratedTemplatesCoverAllCategories() {
        val categories = ChallengeCatalog.templates.map { it.category }.toSet()
        assertTrue(categories.contains(CompanionCategory.HAPPINESS))
        assertTrue(categories.contains(CompanionCategory.ENERGY))
        assertTrue(categories.contains(CompanionCategory.WISDOM))
        assertTrue(categories.contains(CompanionCategory.CREATIVITY))
        assertTrue(ChallengeCatalog.templates.size >= 12)
    }

    @Test
    fun testGenerateDailyChallengesDeterministic() {
        val dateString = "2026-09-02"
        val batch1 = ChallengeCatalog.generateDailyChallenges(dateString, "user-123")
        val batch2 = ChallengeCatalog.generateDailyChallenges(dateString, "user-123")

        assertEquals(3, batch1.size)
        assertEquals(batch1, batch2)

        batch1.forEach { challenge ->
            assertEquals(dateString, challenge.dateString)
            assertEquals("user-123", challenge.userId)
            assertFalse(challenge.completed)
            assertTrue(challenge.xpReward >= 20)
            assertTrue(challenge.title.isNotBlank())
            assertTrue(challenge.description.isNotBlank())
        }
    }

    @Test
    fun testGenerateDailyChallengesDifferentDatesDistinct() {
        val date1 = "2026-09-02"
        val date2 = "2026-09-03"

        val batch1 = ChallengeCatalog.generateDailyChallenges(date1)
        val batch2 = ChallengeCatalog.generateDailyChallenges(date2)

        assertEquals(3, batch1.size)
        assertEquals(3, batch2.size)
        // IDs must reflect the specific date
        assertTrue(batch1.all { it.dateString == date1 })
        assertTrue(batch2.all { it.dateString == date2 })
    }
}
