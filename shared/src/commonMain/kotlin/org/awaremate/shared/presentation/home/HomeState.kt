package org.awaremate.shared.presentation.home

import org.awaremate.shared.domain.model.AwarenessScore
import org.awaremate.shared.domain.model.AwarenessScoreCalculator
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.MomentumTier
import org.awaremate.shared.domain.usecase.companion.CompanionGrowthMetrics

import org.awaremate.shared.domain.usecase.sunset.SunsetStage
import org.awaremate.shared.domain.usecase.sunset.SunsetStatus

data class HomeState(
    val companion: Companion = Companion(),
    val growthMetrics: CompanionGrowthMetrics? = null,
    val momentumTier: MomentumTier = MomentumTier.SPARKING,
    val isComebackBonusActive: Boolean = false,
    val awarenessScore: AwarenessScore = AwarenessScoreCalculator.calculate(120, 180, 25, 1, 1),
    val dailyChallenges: List<DailyChallenge> = emptyList(),
    val sunsetStatus: SunsetStatus = SunsetStatus(SunsetStage.DAYTIME, 0, 0, 21, 45, ""),
    val screenTimeMinutes: Int = 120,
    val screenTimeGoalMinutes: Int = 180,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

sealed interface HomeIntent {
    data object LoadDashboard : HomeIntent
    data object WaterPlant : HomeIntent
    data class CompleteChallenge(val challenge: DailyChallenge) : HomeIntent
    data object ClearSnackbar : HomeIntent
}
