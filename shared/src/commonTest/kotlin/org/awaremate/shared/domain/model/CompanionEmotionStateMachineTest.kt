package org.awaremate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CompanionEmotionStateMachineTest {

    @Test
    fun testFocusSessionTriggersCheerful() {
        val next = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.PEACEFUL,
            event = CompanionEvent.FocusSessionCompleted(durationMinutes = 25)
        )
        assertEquals(CompanionEmotion.CHEERFUL, next)
    }

    @Test
    fun testMoodLoggedTriggersPeaceful() {
        val next = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.TIRED,
            event = CompanionEvent.MoodLogged(moodScore = 4)
        )
        assertEquals(CompanionEmotion.PEACEFUL, next)
    }

    @Test
    fun testBreathExerciseTriggersPeaceful() {
        val next = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.CURIOUS,
            event = CompanionEvent.BreathExerciseCompleted
        )
        assertEquals(CompanionEmotion.PEACEFUL, next)
    }

    @Test
    fun testChallengeCompletedAndEvolvedTriggerCheerful() {
        val challengeNext = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.PEACEFUL,
            event = CompanionEvent.ChallengeCompleted(CompanionCategory.WISDOM)
        )
        assertEquals(CompanionEmotion.CHEERFUL, challengeNext)

        val evolvedNext = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.RESTING,
            event = CompanionEvent.Evolved(CompanionStage.SPROUT)
        )
        assertEquals(CompanionEmotion.CHEERFUL, evolvedNext)
    }

    @Test
    fun testInactivityDecayTransitionsToResting() {
        // 1 day inactive -> remains peaceful
        val oneDay = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.PEACEFUL,
            event = CompanionEvent.InactivityDecay(daysInactive = 1)
        )
        assertEquals(CompanionEmotion.PEACEFUL, oneDay)

        // 2+ days inactive -> gently transitions to RESTING (peaceful sleep, no guilt)
        val twoDays = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.PEACEFUL,
            event = CompanionEvent.InactivityDecay(daysInactive = 2)
        )
        assertEquals(CompanionEmotion.RESTING, twoDays)
    }

    @Test
    fun testAppOpenedAwakensCompanionToCurious() {
        // Long absence (> 24 hours) -> CURIOUS greeting
        val afterLongAbsence = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.PEACEFUL,
            event = CompanionEvent.AppOpened(hoursSinceLastActive = 36)
        )
        assertEquals(CompanionEmotion.CURIOUS, afterLongAbsence)

        // Waking up from RESTING -> CURIOUS
        val fromResting = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.RESTING,
            event = CompanionEvent.AppOpened(hoursSinceLastActive = 5)
        )
        assertEquals(CompanionEmotion.CURIOUS, fromResting)

        // Regular intra-day open -> maintains current emotion
        val regularOpen = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.CHEERFUL,
            event = CompanionEvent.AppOpened(hoursSinceLastActive = 2)
        )
        assertEquals(CompanionEmotion.CHEERFUL, regularOpen)
    }

    @Test
    fun testBedtimeApproachingTransitionsToTired() {
        val pastBedtime = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.CHEERFUL,
            event = CompanionEvent.BedtimeApproaching(isPastBedtime = true)
        )
        assertEquals(CompanionEmotion.TIRED, pastBedtime)

        val beforeBedtime = CompanionEmotionStateMachine.transition(
            currentEmotion = CompanionEmotion.CHEERFUL,
            event = CompanionEvent.BedtimeApproaching(isPastBedtime = false)
        )
        assertEquals(CompanionEmotion.CHEERFUL, beforeBedtime)
    }
}
