package org.awaremate.shared.domain.model

sealed class CompanionEvent {
    data class FocusSessionCompleted(val durationMinutes: Int) : CompanionEvent()
    data class MoodLogged(val moodScore: Int) : CompanionEvent()
    data class ChallengeCompleted(val category: CompanionCategory) : CompanionEvent()
    data object BreathExerciseCompleted : CompanionEvent()
    data class AppOpened(val hoursSinceLastActive: Long) : CompanionEvent()
    data class BedtimeApproaching(val isPastBedtime: Boolean) : CompanionEvent()
    data class InactivityDecay(val daysInactive: Int) : CompanionEvent()
    data class Evolved(val newStage: CompanionStage) : CompanionEvent()
}

object CompanionEmotionStateMachine {

    /**
     * Pure state transition function.
     * Guaranteed non-punitive: companion never enters negative or guilt-inducing states.
     */
    fun transition(
        currentEmotion: CompanionEmotion,
        event: CompanionEvent
    ): CompanionEmotion = when (event) {
        is CompanionEvent.FocusSessionCompleted -> CompanionEmotion.CHEERFUL
        is CompanionEvent.MoodLogged -> CompanionEmotion.PEACEFUL
        is CompanionEvent.ChallengeCompleted -> CompanionEmotion.CHEERFUL
        is CompanionEvent.BreathExerciseCompleted -> CompanionEmotion.PEACEFUL
        is CompanionEvent.Evolved -> CompanionEmotion.CHEERFUL

        is CompanionEvent.AppOpened -> {
            if (event.hoursSinceLastActive >= 24L || currentEmotion == CompanionEmotion.RESTING) {
                CompanionEmotion.CURIOUS
            } else {
                currentEmotion
            }
        }

        is CompanionEvent.BedtimeApproaching -> {
            if (event.isPastBedtime) {
                CompanionEmotion.TIRED
            } else {
                currentEmotion
            }
        }

        is CompanionEvent.InactivityDecay -> {
            if (event.daysInactive >= 2) {
                CompanionEmotion.RESTING
            } else {
                currentEmotion
            }
        }
    }
}
