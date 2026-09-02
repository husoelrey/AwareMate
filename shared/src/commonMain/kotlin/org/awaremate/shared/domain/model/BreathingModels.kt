package org.awaremate.shared.domain.model

enum class BreathingPattern(
    val title: String,
    val description: String,
    val inhaleSeconds: Int,
    val holdInSeconds: Int,
    val exhaleSeconds: Int,
    val holdOutSeconds: Int
) {
    BOX_BREATHING(
        title = "Box Breathing",
        description = "Equal 4-second intervals for mental clarity and nervous system equilibrium.",
        inhaleSeconds = 4,
        holdInSeconds = 4,
        exhaleSeconds = 4,
        holdOutSeconds = 4
    ),
    RELAXING_4_7_8(
        title = "4-7-8 Deep Calm",
        description = "Deep restorative pacing designed to soothe tension and quiet a busy mind.",
        inhaleSeconds = 4,
        holdInSeconds = 7,
        exhaleSeconds = 8,
        holdOutSeconds = 0
    ),
    GROUNDING_CALM(
        title = "Grounding Reset",
        description = "Simple extended exhale to quickly ground body and mind during a busy day.",
        inhaleSeconds = 4,
        holdInSeconds = 0,
        exhaleSeconds = 6,
        holdOutSeconds = 0
    );

    val totalCycleSeconds: Int
        get() = inhaleSeconds + holdInSeconds + exhaleSeconds + holdOutSeconds
}

enum class BreathingPhase(
    val instruction: String,
    val cue: String
) {
    INHALE("Breathe In", "Feel your chest and belly expand softly"),
    HOLD_IN("Hold", "Rest in the fullness of your breath"),
    EXHALE("Breathe Out", "Slowly release all tension"),
    HOLD_OUT("Rest", "Embrace the stillness before the next breath")
}

data class BreathingSessionState(
    val pattern: BreathingPattern = BreathingPattern.BOX_BREATHING,
    val currentPhase: BreathingPhase = BreathingPhase.INHALE,
    val secondsRemainingInPhase: Int = 4,
    val phaseProgress: Float = 0f, // 0.0 to 1.0 within the current phase
    val currentCycle: Int = 1,
    val targetCycles: Int = 4,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false
)
