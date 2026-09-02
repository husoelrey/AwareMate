package org.awaremate.shared.domain.model

object CompanionGrowthRules {
    const val SEED_THRESHOLD_XP = 0
    const val SPROUT_THRESHOLD_XP = 100
    const val BLOOM_THRESHOLD_XP = 300
    const val TREE_THRESHOLD_XP = 600
    const val ANCIENT_TREE_THRESHOLD_XP = 1000

    fun getStageForXp(totalXp: Int): CompanionStage = when {
        totalXp >= ANCIENT_TREE_THRESHOLD_XP -> CompanionStage.ANCIENT_TREE
        totalXp >= TREE_THRESHOLD_XP -> CompanionStage.TREE
        totalXp >= BLOOM_THRESHOLD_XP -> CompanionStage.BLOOM
        totalXp >= SPROUT_THRESHOLD_XP -> CompanionStage.SPROUT
        else -> CompanionStage.SEED
    }

    fun getXpRequiredForStage(stage: CompanionStage): Int = when (stage) {
        CompanionStage.SEED -> SEED_THRESHOLD_XP
        CompanionStage.SPROUT -> SPROUT_THRESHOLD_XP
        CompanionStage.BLOOM -> BLOOM_THRESHOLD_XP
        CompanionStage.TREE -> TREE_THRESHOLD_XP
        CompanionStage.ANCIENT_TREE -> ANCIENT_TREE_THRESHOLD_XP
    }

    fun getNextStage(stage: CompanionStage): CompanionStage? = when (stage) {
        CompanionStage.SEED -> CompanionStage.SPROUT
        CompanionStage.SPROUT -> CompanionStage.BLOOM
        CompanionStage.BLOOM -> CompanionStage.TREE
        CompanionStage.TREE -> CompanionStage.ANCIENT_TREE
        CompanionStage.ANCIENT_TREE -> null
    }

    fun getXpRequiredForNextStage(stage: CompanionStage): Int? {
        val next = getNextStage(stage) ?: return null
        return getXpRequiredForStage(next)
    }

    /**
     * Calculates the normalized progress (0.0 to 1.0) within the companion's current stage.
     * If already at ANCIENT_TREE (max stage), returns 1.0f.
     */
    fun getProgressWithinStage(totalXp: Int): Float {
        val currentStage = getStageForXp(totalXp)
        val currentStageBaseXp = getXpRequiredForStage(currentStage)
        val nextStageXp = getXpRequiredForNextStage(currentStage) ?: return 1.0f

        val stageSpan = (nextStageXp - currentStageBaseXp).coerceAtLeast(1)
        val progressXp = (totalXp - currentStageBaseXp).coerceAtLeast(0)
        return (progressXp.toFloat() / stageSpan.toFloat()).coerceIn(0.0f, 1.0f)
    }

    /**
     * Calculates remaining XP needed to evolve to the next stage.
     * Returns 0 if already at maximum stage (ANCIENT_TREE).
     */
    fun getRemainingXpForNextStage(totalXp: Int): Int {
        val currentStage = getStageForXp(totalXp)
        val nextStageXp = getXpRequiredForNextStage(currentStage) ?: return 0
        return (nextStageXp - totalXp).coerceAtLeast(0)
    }

    /**
     * Checks if increasing XP from [oldXp] to [newXp] triggers a stage evolution.
     */
    fun isStageEvolution(oldXp: Int, newXp: Int): Boolean {
        val oldStage = getStageForXp(oldXp)
        val newStage = getStageForXp(newXp)
        return newStage.ordinal > oldStage.ordinal
    }
}
