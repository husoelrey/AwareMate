package org.awaremate.shared.domain.model

import kotlin.math.abs

data class ChallengeTemplate(
    val title: String,
    val description: String,
    val category: CompanionCategory,
    val xpReward: Int = 25
)

object ChallengeCatalog {

    val templates: List<ChallengeTemplate> = listOf(
        // HAPPINESS
        ChallengeTemplate(
            title = "Gratitude Spark",
            description = "Note down 3 things you are genuinely grateful for today.",
            category = CompanionCategory.HAPPINESS,
            xpReward = 20
        ),
        ChallengeTemplate(
            title = "Kindness Nudge",
            description = "Send an uplifting message or say thank you to a friend or mentor.",
            category = CompanionCategory.HAPPINESS,
            xpReward = 25
        ),
        ChallengeTemplate(
            title = "Unplugged Warmth",
            description = "Enjoy a cup of tea or a healthy snack without your phone in hand.",
            category = CompanionCategory.HAPPINESS,
            xpReward = 20
        ),
        ChallengeTemplate(
            title = "Appreciation Whisper",
            description = "Express genuine appreciation to someone for something small they did.",
            category = CompanionCategory.HAPPINESS,
            xpReward = 25
        ),

        // ENERGY
        ChallengeTemplate(
            title = "Morning Motion",
            description = "Start your day with 5 minutes of gentle stretching or yoga.",
            category = CompanionCategory.ENERGY,
            xpReward = 20
        ),
        ChallengeTemplate(
            title = "Nature Step",
            description = "Step outside for a 10-minute mindful walk in natural light.",
            category = CompanionCategory.ENERGY,
            xpReward = 25
        ),
        ChallengeTemplate(
            title = "Hydration First",
            description = "Drink a full glass of water before opening any social media feeds.",
            category = CompanionCategory.ENERGY,
            xpReward = 20
        ),
        ChallengeTemplate(
            title = "Sky Gaze Pause",
            description = "Spend 3 uninterrupted minutes observing clouds or stars through a window.",
            category = CompanionCategory.ENERGY,
            xpReward = 20
        ),

        // WISDOM
        ChallengeTemplate(
            title = "Deep Focus Sprint",
            description = "Complete a 25-minute focused session with notifications silenced.",
            category = CompanionCategory.WISDOM,
            xpReward = 30
        ),
        ChallengeTemplate(
            title = "Physical Reading",
            description = "Read 10 pages of a physical book or insightful article.",
            category = CompanionCategory.WISDOM,
            xpReward = 25
        ),
        ChallengeTemplate(
            title = "Digital Sunset Prep",
            description = "Enable Do Not Disturb or grayscale mode 1 hour before sleep.",
            category = CompanionCategory.WISDOM,
            xpReward = 25
        ),
        ChallengeTemplate(
            title = "Single-Task Flow",
            description = "Complete one single chore or study task with all browser tabs closed.",
            category = CompanionCategory.WISDOM,
            xpReward = 25
        ),

        // CREATIVITY
        ChallengeTemplate(
            title = "Mindful Doodle",
            description = "Spend 5 minutes sketching or doodling an object in your room.",
            category = CompanionCategory.CREATIVITY,
            xpReward = 25
        ),
        ChallengeTemplate(
            title = "Offline Hobby Time",
            description = "Dedicate 15 minutes to an offline craft, instrument, or cooking.",
            category = CompanionCategory.CREATIVITY,
            xpReward = 30
        ),
        ChallengeTemplate(
            title = "Fresh Idea Log",
            description = "Write down one creative idea or dream in your personal notebook.",
            category = CompanionCategory.CREATIVITY,
            xpReward = 20
        ),
        ChallengeTemplate(
            title = "Sensory Exploration",
            description = "Notice and write down 3 distinct natural textures you touch today.",
            category = CompanionCategory.CREATIVITY,
            xpReward = 20
        )
    )

    /**
     * Generates a deterministic list of 3 daily challenges for a given [dateString] (e.g. "2026-09-02").
     */
    fun generateDailyChallenges(
        dateString: String,
        userId: String = "primary"
    ): List<DailyChallenge> {
        val dateHash = abs(dateString.hashCode())
        val allCategories = CompanionCategory.entries

        // Pick 3 categories rotating deterministically by date
        val categoryOffset = dateHash % allCategories.size
        val selectedCategories = (0 until 3).map { index ->
            allCategories[(categoryOffset + index) % allCategories.size]
        }

        return selectedCategories.mapIndexed { index, category ->
            val pool = templates.filter { it.category == category }
            val templateIndex = (dateHash + index) % pool.size
            val template = pool[templateIndex]

            DailyChallenge(
                id = "challenge_${dateString}_${category.name.lowercase()}",
                userId = userId,
                title = template.title,
                description = template.description,
                category = template.category,
                xpReward = template.xpReward,
                dateString = dateString,
                completed = false,
                completedAtEpochMs = null,
                isSynced = false
            )
        }
    }
}
