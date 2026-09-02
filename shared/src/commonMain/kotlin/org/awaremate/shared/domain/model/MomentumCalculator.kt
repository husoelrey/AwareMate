package org.awaremate.shared.domain.model

import kotlin.math.pow

enum class MomentumTier {
    SPARKING,
    FLOWING,
    STEADY,
    RESTING
}

object MomentumCalculator {
    const val MAX_MOMENTUM = 100.0
    const val MIN_MOMENTUM = 0.0
    const val DEFAULT_INITIAL_MOMENTUM = 100.0
    const val DAILY_DECAY_RATE = 0.90
    const val COMEBACK_BONUS_MULTIPLIER = 1.5
    const val INACTIVITY_COMEBACK_THRESHOLD_DAYS = 2

    // Base activity boosts
    const val BASE_ACTIVITY_BOOST = 10.0
    const val MOOD_LOG_BOOST = 8.0
    const val CHALLENGE_COMPLETION_BOOST = 12.0
    const val FOCUS_SESSION_BASE_BOOST = 10.0
    const val BREATH_EXERCISE_BOOST = 8.0

    /**
     * Calculates the decayed momentum score given the number of missed/inactive days.
     * Uses non-punitive gradual exponential decay: score * (0.90 ^ daysInactive).
     * Missing days never drops the score abruptly to zero.
     */
    fun calculateDecayedScore(currentScore: Double, daysInactive: Int): Double {
        if (daysInactive <= 0) {
            return currentScore.coerceIn(MIN_MOMENTUM, MAX_MOMENTUM)
        }
        val clampedScore = currentScore.coerceIn(MIN_MOMENTUM, MAX_MOMENTUM)
        val decayFactor = DAILY_DECAY_RATE.pow(daysInactive.toDouble())
        val decayed = clampedScore * decayFactor
        return (decayed).coerceIn(MIN_MOMENTUM, MAX_MOMENTUM)
    }

    /**
     * Calculates updated momentum score after user performs a positive activity.
     * Applies a 1.5x Comeback Bonus if the user was inactive for >= 2 days.
     */
    fun calculateBoostedScore(
        currentScore: Double,
        baseBoost: Double = BASE_ACTIVITY_BOOST,
        daysInactive: Int = 0
    ): Double {
        val multiplier = if (daysInactive >= INACTIVITY_COMEBACK_THRESHOLD_DAYS) {
            COMEBACK_BONUS_MULTIPLIER
        } else {
            1.0
        }
        val effectiveGain = baseBoost * multiplier
        val newScore = currentScore + effectiveGain
        return newScore.coerceIn(MIN_MOMENTUM, MAX_MOMENTUM)
    }

    /**
     * Maps a momentum score to a descriptive, compassionate momentum tier.
     */
    fun getTierForScore(score: Double): MomentumTier = when {
        score >= 80.0 -> MomentumTier.SPARKING
        score >= 50.0 -> MomentumTier.FLOWING
        score >= 25.0 -> MomentumTier.STEADY
        else -> MomentumTier.RESTING
    }
}
