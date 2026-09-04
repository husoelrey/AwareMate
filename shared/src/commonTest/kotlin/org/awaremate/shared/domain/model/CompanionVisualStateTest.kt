package org.awaremate.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CompanionVisualStateTest {
    @Test
    fun visualStateUsesCurrentCompanionStageAndEmotion() {
        val visual = Companion(
            stage = CompanionStage.BLOOM,
            emotion = CompanionEmotion.CHEERFUL
        ).toVisualState()

        assertEquals("🌸", visual.stageGlyph)
        assertEquals("Bloom", visual.stageLabel)
        assertEquals("😊", visual.emotionGlyph)
        assertEquals("Cheerful", visual.emotionLabel)
    }
}
