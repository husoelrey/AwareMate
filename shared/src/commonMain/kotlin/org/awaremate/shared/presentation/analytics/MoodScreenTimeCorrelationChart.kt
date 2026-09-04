package org.awaremate.shared.presentation.analytics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimePoint

@Composable
expect fun MoodScreenTimeCorrelationChart(
    data: List<WeeklyMoodScreenTimePoint>,
    modifier: Modifier = Modifier
)
