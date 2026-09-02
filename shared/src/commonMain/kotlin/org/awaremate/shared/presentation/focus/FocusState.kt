package org.awaremate.shared.presentation.focus

import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.model.FocusCategory
import org.awaremate.shared.domain.model.FocusSession

enum class FocusTimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class FocusState(
    val status: FocusTimerStatus = FocusTimerStatus.IDLE,
    val selectedDurationMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val selectedCategory: FocusCategory = FocusCategory.DEEP_WORK,
    val companionStage: CompanionStage = CompanionStage.SPROUT,
    val companionEmotion: CompanionEmotion = CompanionEmotion.PEACEFUL,
    val earnedXp: Int = 0,
    val totalFocusMinutesToday: Long = 0L,
    val recentSessions: List<FocusSession> = emptyList(),
    val showCelebrationDialog: Boolean = false,
    val targetEndTimeEpochMs: Long? = null
) {
    val progress: Float
        get() {
            val totalSec = selectedDurationMinutes * 60
            if (totalSec <= 0) return 0f
            return ((totalSec - remainingSeconds).toFloat() / totalSec).coerceIn(0f, 1f)
        }

    val formattedRemainingTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val minStr = if (minutes < 10) "0$minutes" else "$minutes"
            val secStr = if (seconds < 10) "0$seconds" else "$seconds"
            return "$minStr:$secStr"
        }
}

sealed interface FocusIntent {
    data class SelectDuration(val minutes: Int) : FocusIntent
    data class SelectCategory(val category: FocusCategory) : FocusIntent
    data object StartTimer : FocusIntent
    data object PauseTimer : FocusIntent
    data object ResumeTimer : FocusIntent
    data object StopTimer : FocusIntent
    data object DismissCelebration : FocusIntent
}
