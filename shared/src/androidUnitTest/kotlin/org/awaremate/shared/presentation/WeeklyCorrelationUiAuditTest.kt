package org.awaremate.shared.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimeCorrelation
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimePoint
import org.awaremate.shared.presentation.growth.components.WeeklyMoodScreenTimeCard
import org.awaremate.shared.presentation.theme.AwareMateTheme
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class WeeklyCorrelationUiAuditTest {

    @Test
    fun threeMoodDaysShowsEncouragingStateWithoutChart() = runComposeUiTest {
        setContent {
            AwareMateTheme(dynamicColor = false) {
                WeeklyMoodScreenTimeCard(
                    moodEntries = emptyList(),
                    correlation = WeeklyMoodScreenTimeCorrelation(
                        hasEnoughMoodDays = false,
                        points = points(3)
                    )
                )
            }
        }

        onNodeWithText("This part of the picture will appear as I add more check-ins.")
            .assertIsDisplayed()
        onAllNodesWithContentDescription(
            "Weekly mood, energy, and screen-time chart",
            substring = true
        ).assertCountEquals(0)
    }

    @Test
    fun fiveMoodDaysRendersChartAndObservationalInsight() = runComposeUiTest {
        setContent {
            AwareMateTheme(dynamicColor = false) {
                WeeklyMoodScreenTimeCard(
                    moodEntries = emptyList(),
                    correlation = WeeklyMoodScreenTimeCorrelation(
                        hasEnoughMoodDays = true,
                        points = points(5),
                        observationalInsight = "Your energy and screen-time patterns varied across the week."
                    )
                )
            }
        }

        onNodeWithContentDescription(
            "Weekly mood, energy, and screen-time chart",
            substring = true
        ).assertIsDisplayed()
        onAllNodesWithText("Your energy and screen-time patterns varied across the week.")
            .assertCountEquals(1)
    }

    private fun points(count: Int) = (1..count).map { day ->
        WeeklyMoodScreenTimePoint(
            dayLabel = "D$day",
            dateString = "2026-09-0$day",
            moodScore = 3,
            energyLevel = 3,
            screenTimeMinutes = day * 30
        )
    }
}
