package org.awaremate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MindfulNudgeRulesTest {

    @Test
    fun testContinuousNudgeSuppressedDuringFocusSession() {
        val triggered = MindfulNudgeRuleEngine.shouldTriggerContinuousNudge(
            continuousUsageMinutes = 45,
            thresholdMinutes = 30,
            lastNudgeEpochMs = 0L,
            currentEpochMs = 3600_000L,
            isFocusSessionActive = true // active focus session
        )
        assertFalse(triggered, "Nudges must be muted during an active focus session")
    }

    @Test
    fun testContinuousNudgeSuppressedWhenBelowThreshold() {
        val triggered = MindfulNudgeRuleEngine.shouldTriggerContinuousNudge(
            continuousUsageMinutes = 20,
            thresholdMinutes = 30,
            lastNudgeEpochMs = 0L,
            currentEpochMs = 3600_000L,
            isFocusSessionActive = false
        )
        assertFalse(triggered, "Nudges must not trigger when usage is under threshold")
    }

    @Test
    fun testContinuousNudgeEnforcesCooldown() {
        val now = 100_000_000L
        val fifteenMinsAgo = now - (15 * 60 * 1000L)

        val triggered = MindfulNudgeRuleEngine.shouldTriggerContinuousNudge(
            continuousUsageMinutes = 45,
            thresholdMinutes = 30,
            lastNudgeEpochMs = fifteenMinsAgo,
            currentEpochMs = now,
            isFocusSessionActive = false,
            cooldownMinutes = 30
        )
        assertFalse(triggered, "Nudges must respect the minimum 30-minute cooldown window")
    }

    @Test
    fun testContinuousNudgeTriggersWhenThresholdReachedAndCooldownElapsed() {
        val now = 100_000_000L
        val fortyMinsAgo = now - (40 * 60 * 1000L)

        val triggered = MindfulNudgeRuleEngine.shouldTriggerContinuousNudge(
            continuousUsageMinutes = 35,
            thresholdMinutes = 30,
            lastNudgeEpochMs = fortyMinsAgo,
            currentEpochMs = now,
            isFocusSessionActive = false,
            cooldownMinutes = 30
        )
        assertTrue(triggered, "Nudge should trigger when threshold reached and cooldown elapsed")
    }

    @Test
    fun testGoalNudgeTriggersWhenScreenTimeExceedsGoal() {
        val triggered = MindfulNudgeRuleEngine.shouldTriggerGoalNudge(
            totalDailyMinutes = 185,
            dailyGoalMinutes = 180,
            alreadyTriggeredToday = false,
            isFocusSessionActive = false
        )
        assertTrue(triggered)
    }

    @Test
    fun testGoalNudgeSuppressedIfAlreadyTriggeredToday() {
        val triggered = MindfulNudgeRuleEngine.shouldTriggerGoalNudge(
            totalDailyMinutes = 200,
            dailyGoalMinutes = 180,
            alreadyTriggeredToday = true,
            isFocusSessionActive = false
        )
        assertFalse(triggered, "Daily goal nudge should trigger only once per day")
    }

    @Test
    fun testCatalogAdheresToAntiGuiltAndCompassionateRules() {
        val forbiddenGuiltWords = listOf(
            "fail", "too much", "wasted", "bad", "shame", "guilt", "punish", "loser", "streak broken"
        )

        val allNudges = MindfulNudgeCatalog.continuousNudges +
                MindfulNudgeCatalog.goalNudges +
                MindfulNudgeCatalog.sunsetNudges

        assertTrue(allNudges.isNotEmpty(), "Catalog should contain predefined mindful messages")

        for (nudge in allNudges) {
            val content = "${nudge.title} ${nudge.body}".lowercase()
            for (forbidden in forbiddenGuiltWords) {
                assertFalse(
                    content.contains(forbidden),
                    "Nudge message '${nudge.id}' contains forbidden guilt word: '$forbidden'"
                )
            }
            assertTrue(nudge.title.isNotBlank())
            assertTrue(nudge.body.isNotBlank())
            assertNotNull(nudge.type)
        }
    }
}
