package org.awaremate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyChallengeTest {

    @Test
    fun testDailyChallengeDefaultsAndCompletion() {
        val challenge = DailyChallenge(
            id = "dc-1",
            userId = "user-1",
            title = "Digital Sunset",
            description = "Put phone down 30 minutes before sleep",
            category = CompanionCategory.WISDOM,
            xpReward = 30,
            dateString = "2026-09-02"
        )

        assertFalse(challenge.completed)
        assertEquals(30, challenge.xpReward)
        assertEquals(CompanionCategory.WISDOM, challenge.category)

        val completed = challenge.copy(
            completed = true,
            completedAtEpochMs = 1725283600000L
        )

        assertTrue(completed.completed)
        assertEquals(1725283600000L, completed.completedAtEpochMs)
    }
}
