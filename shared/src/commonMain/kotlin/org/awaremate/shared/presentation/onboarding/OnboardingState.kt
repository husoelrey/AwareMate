package org.awaremate.shared.presentation.onboarding

enum class OnboardingStep(val stepNumber: Int, val totalSteps: Int = 5) {
    WELCOME(1),
    INTERESTS(2),
    COMPANION_NAMING(3),
    PERMISSIONS(4),
    INTENTIONS(5)
}

enum class UserInterest(val title: String, val description: String, val icon: String) {
    DIGITAL_BALANCE("Digital Balance", "Cultivate mindful screen time habits", "🌿"),
    DEEP_FOCUS("Deep Focus", "Build undistracted focus rhythms", "🎯"),
    EMOTIONAL_WELLBEING("Emotional Wellbeing", "Reflect on daily moods & energy", "💛"),
    CREATIVE_PURSUITS("Creative Pursuits", "Discover offline hobbies and crafts", "🎨"),
    RESTFUL_SLEEP("Restful Sleep", "Wind down with gentle digital sunsets", "🌙")
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val selectedInterests: Set<UserInterest> = setOf(UserInterest.DIGITAL_BALANCE, UserInterest.DEEP_FOCUS),
    val companionName: String = "Sprout",
    val notificationsEnabled: Boolean = true,
    val dailyScreenTimeGoalMinutes: Int = 180,
    val nudgeThresholdMinutes: Int = 30,
    val bedtimeHour: Int = 22,
    val bedtimeMinute: Int = 30,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
