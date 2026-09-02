package org.awaremate.shared.presentation.growth

import org.awaremate.shared.domain.model.BreathingSessionState
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCategory
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.SelfDiscoveryPrompt
import org.awaremate.shared.domain.model.WeeklyMoodInsights

data class GrowthState(
    val isLoading: Boolean = true,
    val recentMoods: List<MoodEntry> = emptyList(),
    val todayMood: MoodEntry? = null,
    val allHobbies: List<Hobby> = emptyList(),
    val recommendedHobbies: List<Hobby> = emptyList(),
    val selectedHobbyCategory: HobbyCategory? = null,
    val prompts: List<SelfDiscoveryPrompt> = emptyList(),
    val currentPromptIndex: Int = 0,
    val dailyChallenges: List<DailyChallenge> = emptyList(),
    val weeklyInsights: WeeklyMoodInsights? = null,
    val breathingState: BreathingSessionState = BreathingSessionState(),
    val isMoodDialogOpen: Boolean = false,
    val isBreathingDialogOpen: Boolean = false,
    val snackbarMessage: String? = null
) {
    val currentPrompt: SelfDiscoveryPrompt?
        get() = if (prompts.isNotEmpty() && currentPromptIndex in prompts.indices) {
            prompts[currentPromptIndex]
        } else null
}
