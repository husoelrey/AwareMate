package org.awaremate.shared.presentation.growth.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimeCorrelation

@Composable
actual fun WeeklyInsightShareSection(
    moodEntries: List<MoodEntry>,
    correlation: WeeklyMoodScreenTimeCorrelation,
    modifier: Modifier
) {
    WeeklyInsightCardContent(moodEntries, correlation, modifier)
}
