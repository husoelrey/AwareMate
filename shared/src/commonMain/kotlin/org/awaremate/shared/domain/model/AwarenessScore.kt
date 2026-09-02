package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

enum class AwarenessTier {
    MINDFUL_MASTER,
    BALANCED_EXPLORER,
    GROWING_BUD,
    GENTLE_START
}

@Serializable
data class AwarenessScore(
    val totalScore: Int,
    val digitalBalanceScore: Int,
    val mindfulnessScore: Int,
    val emotionalScore: Int,
    val growthScore: Int,
    val tier: AwarenessTier
)

object AwarenessScoreCalculator {
    const val MAX_DIGITAL_BALANCE_SCORE = 30
    const val MAX_MINDFULNESS_SCORE = 25
    const val MAX_EMOTIONAL_SCORE = 25
    const val MAX_GROWTH_SCORE = 20
    const val MAX_TOTAL_SCORE = 100

    /**
     * Calculates the composite awareness score (0..100) from key well-being metrics.
     *
     * @param screenTimeMinutes Actual screen time in minutes today.
     * @param targetScreenTimeGoalMinutes User's daily screen time goal in minutes.
     * @param focusSessionMinutes Total focus/deep work minutes logged today.
     * @param moodEntriesCount Number of mood check-ins completed today.
     * @param completedChallengesCount Number of daily challenges completed today.
     */
    fun calculate(
        screenTimeMinutes: Int,
        targetScreenTimeGoalMinutes: Int,
        focusSessionMinutes: Int,
        moodEntriesCount: Int,
        completedChallengesCount: Int
    ): AwarenessScore {
        // 1. Digital Balance (0..30 pts)
        val digitalBalance = if (targetScreenTimeGoalMinutes <= 0) {
            MAX_DIGITAL_BALANCE_SCORE
        } else {
            val ratio = screenTimeMinutes.toDouble() / targetScreenTimeGoalMinutes.toDouble()
            when {
                ratio <= 1.0 -> MAX_DIGITAL_BALANCE_SCORE
                ratio <= 1.5 -> (MAX_DIGITAL_BALANCE_SCORE * (1.5 - ratio) / 0.5).roundToInt().coerceAtLeast(10)
                ratio <= 2.0 -> (10 * (2.0 - ratio) / 0.5).roundToInt().coerceAtLeast(0)
                else -> 0
            }
        }.coerceIn(0, MAX_DIGITAL_BALANCE_SCORE)

        // 2. Mindfulness & Focus (0..25 pts) - 1 pt per minute, max 25 pts
        val mindfulness = focusSessionMinutes.coerceIn(0, MAX_MINDFULNESS_SCORE)

        // 3. Emotional Check-in (0..25 pts) - 1 entry = 20 pts, 2+ entries = 25 pts
        val emotional = when {
            moodEntriesCount >= 2 -> MAX_EMOTIONAL_SCORE
            moodEntriesCount == 1 -> 20
            else -> 0
        }.coerceIn(0, MAX_EMOTIONAL_SCORE)

        // 4. Growth & Challenges (0..20 pts) - 1 challenge = 10 pts, 2 = 16 pts, 3+ = 20 pts
        val growth = when {
            completedChallengesCount >= 3 -> MAX_GROWTH_SCORE
            completedChallengesCount == 2 -> 16
            completedChallengesCount == 1 -> 10
            else -> 0
        }.coerceIn(0, MAX_GROWTH_SCORE)

        val total = (digitalBalance + mindfulness + emotional + growth).coerceIn(0, MAX_TOTAL_SCORE)

        val tier = when {
            total >= 80 -> AwarenessTier.MINDFUL_MASTER
            total >= 60 -> AwarenessTier.BALANCED_EXPLORER
            total >= 35 -> AwarenessTier.GROWING_BUD
            else -> AwarenessTier.GENTLE_START
        }

        return AwarenessScore(
            totalScore = total,
            digitalBalanceScore = digitalBalance,
            mindfulnessScore = mindfulness,
            emotionalScore = emotional,
            growthScore = growth,
            tier = tier
        )
    }
}
