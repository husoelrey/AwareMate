package org.awaremate.shared.presentation.growth

import org.awaremate.shared.domain.model.BreathingPattern
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCategory

sealed class GrowthIntent {
    data object LoadGrowthData : GrowthIntent()

    // Mood Check-in
    data object OpenMoodDialog : GrowthIntent()
    data object DismissMoodDialog : GrowthIntent()
    data class SubmitMood(
        val emoji: String,
        val moodScore: Int,
        val energyLevel: Int,
        val note: String?,
        val tags: List<String>
    ) : GrowthIntent()

    // Breathing Guide
    data object OpenBreathingDialog : GrowthIntent()
    data object DismissBreathingDialog : GrowthIntent()
    data class StartBreathing(
        val pattern: BreathingPattern = BreathingPattern.BOX_BREATHING,
        val targetCycles: Int = 4
    ) : GrowthIntent()
    data object PauseBreathing : GrowthIntent()
    data object ResumeBreathing : GrowthIntent()
    data object StopBreathing : GrowthIntent()

    // Hobbies
    data class ToggleHobbyBookmark(val hobbyId: String, val isBookmarked: Boolean) : GrowthIntent()
    data class CompleteHobbySession(val hobby: Hobby) : GrowthIntent()
    data class SelectHobbyCategory(val category: HobbyCategory?) : GrowthIntent()

    // Self-Discovery Prompts
    data object NextSelfDiscoveryPrompt : GrowthIntent()
    data object PreviousSelfDiscoveryPrompt : GrowthIntent()
    data class AcknowledgeSelfDiscovery(val promptId: String, val reflection: String?) : GrowthIntent()

    // Micro-Challenges
    data class CompleteChallenge(val challenge: DailyChallenge) : GrowthIntent()

    data object ClearSnackbar : GrowthIntent()
}
