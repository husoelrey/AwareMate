package org.awaremate.shared.domain.model

data class CompanionVisualState(
    val stageGlyph: String,
    val stageLabel: String,
    val emotionGlyph: String,
    val emotionLabel: String
)

fun Companion.toVisualState(): CompanionVisualState = CompanionVisualState(
    stageGlyph = when (stage) {
        CompanionStage.SEED -> "🌰"
        CompanionStage.SPROUT -> "🌱"
        CompanionStage.BLOOM -> "🌸"
        CompanionStage.TREE -> "🌳"
        CompanionStage.ANCIENT_TREE -> "🌲"
    },
    stageLabel = stage.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
    emotionGlyph = when (emotion) {
        CompanionEmotion.PEACEFUL -> "😌"
        CompanionEmotion.CURIOUS -> "🤔"
        CompanionEmotion.CHEERFUL -> "😊"
        CompanionEmotion.TIRED -> "😴"
        CompanionEmotion.RESTING -> "💤"
    },
    emotionLabel = emotion.name.lowercase().replaceFirstChar { it.uppercase() }
)
