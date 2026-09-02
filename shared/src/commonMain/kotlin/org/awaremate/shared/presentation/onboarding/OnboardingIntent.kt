package org.awaremate.shared.presentation.onboarding

sealed interface OnboardingIntent {
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent
    data class ToggleInterest(val interest: UserInterest) : OnboardingIntent
    data class SetCompanionName(val name: String) : OnboardingIntent
    data class SetNotificationsEnabled(val enabled: Boolean) : OnboardingIntent
    data class SetScreenTimeGoal(val minutes: Int) : OnboardingIntent
    data class SetNudgeThreshold(val minutes: Int) : OnboardingIntent
    data class SetBedtime(val hour: Int, val minute: Int) : OnboardingIntent
    data object FinishOnboarding : OnboardingIntent
}
