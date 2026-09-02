package org.awaremate.shared.presentation.analytics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.awaremate.shared.domain.model.DailyScreenTimeData

/**
 * Multiplatform Bar Chart for rendering daily screen time data vs. daily goal.
 */
@Composable
expect fun ScreenTimeBarChart(
    data: List<DailyScreenTimeData>,
    dailyGoalMinutes: Int,
    modifier: Modifier = Modifier
)
