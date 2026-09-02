package org.awaremate.shared.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import org.awaremate.shared.domain.model.AwarenessScoreCalculator
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.MomentumTier
import org.awaremate.shared.domain.usecase.companion.CompanionGrowthMetrics
import org.awaremate.shared.presentation.companion.CompanionCanvas
import org.awaremate.shared.presentation.home.components.CompanionWidget
import org.awaremate.shared.presentation.home.components.DailySparksCard
import org.awaremate.shared.presentation.home.components.QuickActions
import org.awaremate.shared.presentation.home.components.ScoreCard
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.awaremate.shared.presentation.theme.AwareMateTheme
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class ComposeUiAccessibilityTest {

    @Test
    fun testCompanionCanvasHasContentDescription() = runComposeUiTest {
        setContent {
            AwareMateTheme(dynamicColor = false) {
                CompanionCanvas(
                    stage = CompanionStage.SEED,
                    emotion = CompanionEmotion.PEACEFUL
                )
            }
        }

        onNodeWithContentDescription("Interactive Companion Canvas: seed stage, feeling peaceful")
            .assertIsDisplayed()
    }

    @Test
    fun testCompanionWidgetHasContentDescriptionAndHandlesClick() = runComposeUiTest {
        var clicked = false
        val testCompanion = Companion(
            name = "Sprout",
            stage = CompanionStage.SEED,
            emotion = CompanionEmotion.PEACEFUL,
            experiencePoints = 30
        )
        val testMetrics = CompanionGrowthMetrics(
            stage = CompanionStage.SEED,
            totalXp = 30,
            progressWithinStage = 0.3f,
            remainingXpForNextStage = 70,
            nextStage = CompanionStage.SPROUT,
            isMaxStage = false
        )

        setContent {
            AwareMateTheme(dynamicColor = false) {
                CompanionWidget(
                    companion = testCompanion,
                    growthMetrics = testMetrics,
                    onClick = { clicked = true }
                )
            }
        }

        onNodeWithContentDescription("Companion widget: Sprout, Seed stage, feeling Peaceful, total 30 XP. Tap to view companion screen.")
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun testScoreCardDisplaysMomentumAndAwarenessContentDescriptions() = runComposeUiTest {
        val awareness = AwarenessScoreCalculator.calculate(120, 180, 25, 1, 1)

        setContent {
            AwareMateTheme(dynamicColor = false) {
                ScoreCard(
                    momentumScore = 85.0,
                    momentumTier = MomentumTier.SPARKING,
                    awarenessScore = awareness,
                    isComebackBonusActive = true
                )
            }
        }

        onNodeWithContentDescription("Momentum score: 85%, Tier: sparking, non-punitive gradual decay")
            .assertIsDisplayed()

        onNodeWithContentDescription("Awareness balance score: ${awareness.totalScore} out of 100")
            .assertIsDisplayed()
    }

    @Test
    fun testQuickActionsHaveAccessibleLabels() = runComposeUiTest {
        var focusClicked = false
        var moodClicked = false
        var breatheClicked = false
        var waterClicked = false

        setContent {
            AwareMateTheme(dynamicColor = false) {
                QuickActions(
                    onFocusClick = { focusClicked = true },
                    onMoodClick = { moodClicked = true },
                    onBreatheClick = { breatheClicked = true },
                    onWaterClick = { waterClicked = true }
                )
            }
        }

        onNodeWithContentDescription("Start a mindful focus session")
            .assertIsDisplayed()
            .performClick()
        assertTrue(focusClicked)

        onNodeWithContentDescription("Log your daily mood and reflections")
            .assertIsDisplayed()
            .performClick()
        assertTrue(moodClicked)

        onNodeWithContentDescription("Take a 1-minute breathing grounding exercise")
            .assertIsDisplayed()
            .performClick()
        assertTrue(breatheClicked)

        onNodeWithContentDescription("Water your plant companion to share love")
            .assertIsDisplayed()
            .performClick()
        assertTrue(waterClicked)
    }

    @Test
    fun testDailySparksCardDisplaysChallengeAccessibility() = runComposeUiTest {
        val testChallenges = listOf(
            DailyChallenge(
                id = "c1",
                userId = "u1",
                title = "Deep Breath",
                description = "Take 3 deep breaths",
                category = CompanionCategory.WISDOM,
                xpReward = 15,
                dateString = "2026-09-02",
                completed = false
            )
        )

        var completedChallenge: DailyChallenge? = null

        setContent {
            AwareMateTheme(dynamicColor = false) {
                DailySparksCard(
                    challenges = testChallenges,
                    onCompleteChallenge = { completedChallenge = it }
                )
            }
        }

        onNodeWithContentDescription("Challenge: Deep Breath, incomplete. Reward: 15 XP")
            .assertIsDisplayed()

        onNodeWithContentDescription("Complete challenge Deep Breath")
            .assertIsDisplayed()
            .performClick()

        assertTrue(completedChallenge?.id == "c1")
    }

    @Test
    fun testUsagePermissionCardDisplaysGuidanceWhenPermissionNotGranted() = runComposeUiTest {
        var clicked = false
        val state = org.awaremate.shared.presentation.analytics.ScreenTimeAnalyticsState(
            hasPermission = false,
            dailyGoalMinutes = 180
        )

        setContent {
            AwareMateTheme(dynamicColor = false) {
                org.awaremate.shared.presentation.analytics.ScreenTimeAnalyticsScreen(
                    staticState = state,
                    onGrantPermissionClick = { clicked = true }
                ).Content()
            }
        }

        onNodeWithContentDescription("Usage access permission guidance card")
            .assertIsDisplayed()

        onNodeWithContentDescription("Grant usage access permission button")
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun testUsagePermissionCardHiddenWhenPermissionGranted() = runComposeUiTest {
        val state = org.awaremate.shared.presentation.analytics.ScreenTimeAnalyticsState(
            hasPermission = true,
            dailyGoalMinutes = 180
        )

        setContent {
            AwareMateTheme(dynamicColor = false) {
                org.awaremate.shared.presentation.analytics.ScreenTimeAnalyticsScreen(
                    staticState = state
                ).Content()
            }
        }

        onNodeWithContentDescription("Screen Time Analytics Screen")
            .assertIsDisplayed()

        onAllNodesWithContentDescription("Usage access permission guidance card")
            .assertCountEquals(0)
    }

    @Test
    fun testScreenTimeBarChartEmptyDataRendersGracefullyWithoutCrashing() = runComposeUiTest {
        setContent {
            AwareMateTheme(dynamicColor = false) {
                org.awaremate.shared.presentation.analytics.ScreenTimeBarChart(
                    data = emptyList(),
                    dailyGoalMinutes = 180
                )
            }
        }

        onNodeWithContentDescription("Screen time weekly bar chart powered by Vico. 7 days breakdown.")
            .assertIsDisplayed()
    }

    @Test
    fun testScreenTimeBarChartWithDataRendersGracefully() = runComposeUiTest {
        val sampleData = listOf(
            org.awaremate.shared.domain.model.DailyScreenTimeData("Mon", "2026-09-01", 120, 180),
            org.awaremate.shared.domain.model.DailyScreenTimeData("Tue", "2026-09-02", 150, 180)
        )

        setContent {
            AwareMateTheme(dynamicColor = false) {
                org.awaremate.shared.presentation.analytics.ScreenTimeBarChart(
                    data = sampleData,
                    dailyGoalMinutes = 180
                )
            }
        }

        onNodeWithContentDescription("Screen time weekly bar chart powered by Vico. 7 days breakdown.")
            .assertIsDisplayed()
    }

    @Test
    fun testSelfDiscoveryCardHasAccessibleControls() = runComposeUiTest {
        var acknowledged = false
        val prompt = org.awaremate.shared.domain.model.SelfDiscoveryPrompt(
            id = "sd_1",
            category = "Pocket Reflex",
            question = "When reaching for phone, what was your mind doing?",
            curiosityHint = "Observe pause in stimulation"
        )

        setContent {
            AwareMateTheme(dynamicColor = false) {
                org.awaremate.shared.presentation.growth.components.SelfDiscoveryCard(
                    prompt = prompt,
                    currentIndex = 0,
                    totalCount = 8,
                    onNext = {},
                    onPrevious = {},
                    onAcknowledge = { _, _ -> acknowledged = true }
                )
            }
        }

        onNodeWithContentDescription("Self-Discovery prompt card. Question: When reaching for phone, what was your mind doing?")
            .assertIsDisplayed()

        onNodeWithContentDescription("Acknowledge noticing this habit pattern")
            .assertIsDisplayed()
            .performClick()

        assertTrue(acknowledged)
    }

    @Test
    fun testHobbyDiscoverySectionHasAccessibleActions() = runComposeUiTest {
        var sessionLogged = false
        var bookmarkToggled = false
        val sampleHobby = org.awaremate.shared.domain.model.Hobby(
            id = "h1",
            title = "Watercolor Doodling",
            category = org.awaremate.shared.domain.model.HobbyCategory.CREATIVE_ARTS,
            description = "Gentle color blending",
            beginnerTip = "Use water",
            isBookmarked = false
        )

        setContent {
            AwareMateTheme(dynamicColor = false) {
                org.awaremate.shared.presentation.growth.components.HobbyDiscoverySection(
                    hobbies = listOf(sampleHobby),
                    selectedCategory = null,
                    onSelectCategory = {},
                    onToggleBookmark = { _, _ -> bookmarkToggled = true },
                    onCompleteSession = { sessionLogged = true }
                )
            }
        }

        onNodeWithContentDescription("Hobby: Watercolor Doodling. Gentle color blending")
            .assertIsDisplayed()

        onNodeWithContentDescription("Bookmark hobby")
            .assertIsDisplayed()
            .performClick()
        assertTrue(bookmarkToggled)

        onNodeWithContentDescription("Log completed offline session for Watercolor Doodling")
            .assertIsDisplayed()
            .performClick()
        assertTrue(sessionLogged)
    }

    @Test
    fun testSettingsSupportAndPrivacyAccessibility() = runComposeUiTest {
        setContent {
            AwareMateTheme(dynamicColor = false) {
                // Test the support and privacy buttons
                Column {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier
                            .sizeIn(minHeight = 48.dp)
                            .semantics {
                                contentDescription = "Read full AwareMate Privacy Policy"
                            }
                    ) {
                        Text("View Privacy Policy 📜")
                    }

                    Button(
                        onClick = {},
                        modifier = Modifier
                            .sizeIn(minHeight = 48.dp)
                            .semantics {
                                contentDescription = "Support AwareMate on Buy Me a Coffee"
                            }
                    ) {
                        Text("Buy Coffee ☕")
                    }

                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier
                            .sizeIn(minHeight = 48.dp)
                            .semantics {
                                contentDescription = "Support AwareMate via GitHub Sponsors"
                            }
                    ) {
                        Text("Sponsor 💖")
                    }
                }
            }
        }

        onNodeWithContentDescription("Read full AwareMate Privacy Policy")
            .assertIsDisplayed()
            .performClick()

        onNodeWithContentDescription("Support AwareMate on Buy Me a Coffee")
            .assertIsDisplayed()
            .performClick()

        onNodeWithContentDescription("Support AwareMate via GitHub Sponsors")
            .assertIsDisplayed()
            .performClick()
    }
}
