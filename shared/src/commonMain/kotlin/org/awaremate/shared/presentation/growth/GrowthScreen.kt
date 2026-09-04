package org.awaremate.shared.presentation.growth

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
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
import org.awaremate.shared.presentation.growth.components.BreathingGuideDialog
import org.awaremate.shared.presentation.growth.components.HobbyDiscoverySection
import org.awaremate.shared.presentation.growth.components.MoodCheckInDialog
import org.awaremate.shared.presentation.growth.components.SelfDiscoveryCard
import org.awaremate.shared.presentation.growth.components.TodaysFeelingCalendar
import org.awaremate.shared.presentation.growth.components.WeeklyMoodInsightsCard
import org.awaremate.shared.presentation.growth.components.WeeklyMoodScreenTimeCard
import org.awaremate.shared.presentation.home.components.DailySparksCard

class GrowthScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<GrowthScreenModel>()
        val state by screenModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(state.snackbarMessage) {
            state.snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                screenModel.handleIntent(GrowthIntent.ClearSnackbar)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Personal Growth",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nurture awareness, rhythm & offline sparks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                    contentDescription = "Personal Growth Screen"
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
                            contentDescription = "Loading personal growth data"
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
                    // 1. Mood Check-in Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Daily mood check-in section"
                            }
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = state.todayMood?.emoji ?: "💛",
                                        fontSize = 26.sp
                                    )
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text(
                                        text = if (state.todayMood != null) "Today's Feeling" else "How are you feeling?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (state.todayMood != null) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Logged Today ✓", fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (state.todayMood != null) {
                                val mood = state.todayMood!!
                                Text(
                                    text = "Energy Battery: ${mood.energyLevel}/5" +
                                        (mood.note?.let { " • \"$it\"" } ?: ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                if (mood.tags.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Present: " + mood.tags.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Today's check-in is saved. You can revisit it in the calendar below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Track your emotional climate without judgement. Every feeling is welcome here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { screenModel.handleIntent(GrowthIntent.OpenMoodDialog) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Log today's mood check-in"
                                    }
                                ) {
                                    Text("Log Mood Check-in (+15 XP)")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Monthly mood calendar
                    TodaysFeelingCalendar(
                        moodEntries = state.moodEntries,
                        selfDiscoveryPrompts = state.prompts
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Breath & Grounding Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Breath and Grounding exercise card"
                            }
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🌬️", fontSize = 26.sp)
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text(
                                        text = "Breath & Grounding",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("1-2 min", fontSize = 11.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Take a mindful pause with animated radial pacing (Box Breathing, 4-7-8, or Grounding Reset).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { screenModel.handleIntent(GrowthIntent.OpenBreathingDialog) },
                                modifier = Modifier.semantics {
                                    contentDescription = "Start guided breathing exercise"
                                }
                            ) {
                                Text("Start Breathing Guide (+20 XP)")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Curiosity-Driven Self-Discovery Prompt Card
                    SelfDiscoveryCard(
                        prompt = state.currentPrompt,
                        currentIndex = state.currentPromptIndex,
                        totalCount = state.prompts.size,
                        onNext = { screenModel.handleIntent(GrowthIntent.NextSelfDiscoveryPrompt) },
                        onPrevious = { screenModel.handleIntent(GrowthIntent.PreviousSelfDiscoveryPrompt) },
                        onAcknowledge = { promptId, reflection ->
                            screenModel.handleIntent(GrowthIntent.AcknowledgeSelfDiscovery(promptId, reflection))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Offline Hobby Discovery & Personalized Suggestions
                    HobbyDiscoverySection(
                        hobbies = if (state.selectedHobbyCategory == null) state.recommendedHobbies else state.allHobbies.filter { it.category == state.selectedHobbyCategory },
                        selectedCategory = state.selectedHobbyCategory,
                        onSelectCategory = { screenModel.handleIntent(GrowthIntent.SelectHobbyCategory(it)) },
                        onToggleBookmark = { id, isBookmarked ->
                            screenModel.handleIntent(GrowthIntent.ToggleHobbyBookmark(id, isBookmarked))
                        },
                        onCompleteSession = { hobby ->
                            screenModel.handleIntent(GrowthIntent.CompleteHobbySession(hobby))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Daily Micro-Challenges
                    DailySparksCard(
                        challenges = state.dailyChallenges,
                        onCompleteChallenge = { challenge ->
                            screenModel.handleIntent(GrowthIntent.CompleteChallenge(challenge))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7. Weekly Mood & Growth Insights
                    WeeklyMoodInsightsCard(insights = state.weeklyInsights)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 8. Private weekly mood/screen-time correlation
                    WeeklyMoodScreenTimeCard(
                        moodEntries = state.moodEntries,
                        correlation = state.weeklyCorrelation
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Dialogs
            if (state.isMoodDialogOpen) {
                MoodCheckInDialog(
                    onDismiss = { screenModel.handleIntent(GrowthIntent.DismissMoodDialog) },
                    onSubmit = { emoji, moodScore, energyLevel, note, tags ->
                        screenModel.handleIntent(
                            GrowthIntent.SubmitMood(
                                emoji = emoji,
                                moodScore = moodScore,
                                energyLevel = energyLevel,
                                note = note,
                                tags = tags
                            )
                        )
                    }
                )
            }

            if (state.isBreathingDialogOpen) {
                BreathingGuideDialog(
                    state = state.breathingState,
                    onStart = { pattern, cycles ->
                        screenModel.handleIntent(GrowthIntent.StartBreathing(pattern, cycles))
                    },
                    onPause = { screenModel.handleIntent(GrowthIntent.PauseBreathing) },
                    onResume = { screenModel.handleIntent(GrowthIntent.ResumeBreathing) },
                    onStop = { screenModel.handleIntent(GrowthIntent.StopBreathing) },
                    onDismiss = { screenModel.handleIntent(GrowthIntent.DismissBreathingDialog) }
                )
            }
        }
    }
}
