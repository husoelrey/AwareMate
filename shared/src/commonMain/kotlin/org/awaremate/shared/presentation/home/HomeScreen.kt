package org.awaremate.shared.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.awaremate.shared.presentation.companion.CompanionScreen
import org.awaremate.shared.presentation.home.components.CompanionWidget
import org.awaremate.shared.presentation.home.components.DailySparksCard
import org.awaremate.shared.presentation.home.components.QuickActions
import org.awaremate.shared.presentation.home.components.ScoreCard
import org.awaremate.shared.presentation.profile.ProfileScreen

class HomeScreen(
    private val onNavigateToTab: ((Int) -> Unit)? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(state.snackbarMessage) {
            state.snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                screenModel.handleIntent(HomeIntent.ClearSnackbar)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "AwareMate",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Welcome back, Mindful Explorer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navigator.push(ProfileScreen()) },
                            modifier = Modifier.semantics {
                                contentDescription = "Open User Profile"
                            }
                        ) {
                            Text(text = "👤", fontSize = 20.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "AwareMate Home Dashboard"
                }
        ) { innerPadding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Loading dashboard data"
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // 1. Companion Widget
                    CompanionWidget(
                        companion = state.companion,
                        growthMetrics = state.growthMetrics,
                        onClick = {
                            if (onNavigateToTab != null) {
                                onNavigateToTab(1) // Companion Tab index
                            } else {
                                navigator.push(CompanionScreen())
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Score Card (Momentum & Awareness)
                    ScoreCard(
                        momentumScore = state.companion.momentumScore,
                        momentumTier = state.momentumTier,
                        awarenessScore = state.awarenessScore,
                        isComebackBonusActive = state.isComebackBonusActive
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Digital Sunset Banner (shown when sunset is approaching, active, or bedtime)
                    if (state.sunsetStatus.stage != org.awaremate.shared.domain.usecase.sunset.SunsetStage.DAYTIME) {
                        org.awaremate.shared.presentation.sunset.DigitalSunsetBanner(status = state.sunsetStatus)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Digital Awareness & Screen Time Quick Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Digital Awareness card. ${state.screenTimeMinutes} minutes screen time today."
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Today's Screen Rhythm",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${state.screenTimeMinutes / 60}h ${state.screenTimeMinutes % 60}m / ${state.screenTimeGoalMinutes / 60}h intention",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(text = "🌱", fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = {
                                        navigator.push(
                                            org.awaremate.shared.presentation.analytics.ScreenTimeAnalyticsScreen()
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics {
                                            contentDescription = "Open detailed screen time analytics"
                                        }
                                ) {
                                    Text("Analytics", fontSize = 13.sp)
                                }

                                androidx.compose.material3.Button(
                                    onClick = {
                                        val sampleReport = org.awaremate.shared.domain.model.WeeklyDigitalAwarenessReport(
                                            startDate = "Mon",
                                            endDate = "Sun",
                                            dailySummaries = listOf(
                                                org.awaremate.shared.domain.model.DailyUsageSummary("Mon", 140 * 60 * 1000L),
                                                org.awaremate.shared.domain.model.DailyUsageSummary("Tue", 155 * 60 * 1000L),
                                                org.awaremate.shared.domain.model.DailyUsageSummary("Wed", 120 * 60 * 1000L),
                                                org.awaremate.shared.domain.model.DailyUsageSummary("Thu", 160 * 60 * 1000L),
                                                org.awaremate.shared.domain.model.DailyUsageSummary("Fri", 145 * 60 * 1000L),
                                                org.awaremate.shared.domain.model.DailyUsageSummary("Sat", 110 * 60 * 1000L),
                                                org.awaremate.shared.domain.model.DailyUsageSummary("Sun", 130 * 60 * 1000L)
                                            ),
                                            totalScreenTimeMinutes = 960,
                                            dailyAverageMinutes = 137,
                                            totalFocusMinutes = 150,
                                            focusSessionsCount = 6,
                                            averageAwarenessScore = 82,
                                            momentumTrend = "Thriving Momentum 🌟",
                                            compassionateInsight = "🌱 You dedicated 150 mindful focus minutes this week! Every intentional pause nurtures your wellbeing and keeps your sprout thriving."
                                        )
                                        navigator.push(
                                            org.awaremate.shared.presentation.report.WeeklyReportScreen(
                                                report = sampleReport,
                                                dailyGoalMinutes = state.screenTimeGoalMinutes
                                            )
                                        )
                                    },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics {
                                            contentDescription = "Open weekly awareness report"
                                        }
                                ) {
                                    Text("Weekly Report", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Quick Actions
                    QuickActions(
                        onFocusClick = { onNavigateToTab?.invoke(2) },
                        onMoodClick = { onNavigateToTab?.invoke(3) },
                        onBreatheClick = {
                            screenModel.handleIntent(HomeIntent.WaterPlant)
                        },
                        onWaterClick = {
                            screenModel.handleIntent(HomeIntent.WaterPlant)
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Daily Sparks & Challenges
                    DailySparksCard(
                        challenges = state.dailyChallenges,
                        onCompleteChallenge = { challenge ->
                            screenModel.handleIntent(HomeIntent.CompleteChallenge(challenge))
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
