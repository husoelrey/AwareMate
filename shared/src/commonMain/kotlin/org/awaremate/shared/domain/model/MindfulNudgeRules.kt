package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
enum class NudgeType {
    CONTINUOUS_USAGE,
    DAILY_GOAL_REACHED,
    POSTURE_BREATHE,
    HYDRATION,
    EVENING_SUNSET
}

@Serializable
data class NudgeMessage(
    val id: String,
    val title: String,
    val body: String,
    val type: NudgeType
)

object MindfulNudgeCatalog {
    val continuousNudges = listOf(
        NudgeMessage(
            id = "continuous_1",
            title = "🌱 Gentle Check-in",
            body = "You've been active for a little while. How about rolling your shoulders or taking a slow, deep breath?",
            type = NudgeType.CONTINUOUS_USAGE
        ),
        NudgeMessage(
            id = "continuous_2",
            title = "💧 Refresh Moment",
            body = "Your eyes have been working hard. Look away toward the horizon for 20 seconds and enjoy a sip of water.",
            type = NudgeType.HYDRATION
        ),
        NudgeMessage(
            id = "continuous_3",
            title = "🌿 Pause with Your Companion",
            body = "Your plant companion is relaxing quietly. Take a moment to notice how your body feels right now.",
            type = NudgeType.POSTURE_BREATHE
        )
    )

    val goalNudges = listOf(
        NudgeMessage(
            id = "goal_1",
            title = "🌤️ Gentle Horizon",
            body = "You've met your daily screen balance intention. Thank yourself for noticing and feel welcome to step into the physical world.",
            type = NudgeType.DAILY_GOAL_REACHED
        ),
        NudgeMessage(
            id = "goal_2",
            title = "🌸 Mindful Completion",
            body = "You've navigated today's digital rhythm with care. Real-world moments are waiting whenever you choose to unplug.",
            type = NudgeType.DAILY_GOAL_REACHED
        )
    )

    val sunsetNudges = listOf(
        NudgeMessage(
            id = "sunset_1",
            title = "🌅 Digital Sunset",
            body = "Twilight is settling in. Consider putting your screens to rest to prepare your space for deep, restorative sleep.",
            type = NudgeType.EVENING_SUNSET
        ),
        NudgeMessage(
            id = "sunset_2",
            title = "🌙 Nighttime Unwind",
            body = "Your companion is already snuggling down. Soften the room's lights and let your thoughts drift peacefully.",
            type = NudgeType.EVENING_SUNSET
        )
    )

    fun getRandomContinuousNudge(): NudgeMessage = continuousNudges[Random.nextInt(continuousNudges.size)]
    fun getRandomGoalNudge(): NudgeMessage = goalNudges[Random.nextInt(goalNudges.size)]
    fun getRandomSunsetNudge(): NudgeMessage = sunsetNudges[Random.nextInt(sunsetNudges.size)]
}

object MindfulNudgeRuleEngine {

    fun shouldTriggerContinuousNudge(
        continuousUsageMinutes: Int,
        thresholdMinutes: Int,
        lastNudgeEpochMs: Long,
        currentEpochMs: Long,
        isFocusSessionActive: Boolean,
        cooldownMinutes: Int = 30
    ): Boolean {
        if (isFocusSessionActive) return false
        if (continuousUsageMinutes < thresholdMinutes) return false
        val elapsedMinutes = (currentEpochMs - lastNudgeEpochMs) / (1000 * 60)
        return elapsedMinutes >= cooldownMinutes
    }

    fun shouldTriggerGoalNudge(
        totalDailyMinutes: Int,
        dailyGoalMinutes: Int,
        alreadyTriggeredToday: Boolean,
        isFocusSessionActive: Boolean
    ): Boolean {
        if (isFocusSessionActive) return false
        if (alreadyTriggeredToday) return false
        return dailyGoalMinutes in 1..totalDailyMinutes
    }
}
