package org.awaremate.shared.presentation.focus

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import org.awaremate.shared.domain.model.FocusCategory
import org.awaremate.shared.presentation.companion.CompanionCanvas

class FocusScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<FocusScreenModel>()
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mindful Focus",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Mindful Focus Session Screen"
                }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Duration selection chips (only editable when IDLE)
                if (state.status == FocusTimerStatus.IDLE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(15, 25, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = state.selectedDurationMinutes == mins,
                                onClick = { screenModel.handleIntent(FocusIntent.SelectDuration(mins)) },
                                label = { Text("${mins}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.semantics {
                                    contentDescription = "$mins minutes duration chip"
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category selector row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        listOf(
                            FocusCategory.DEEP_WORK to "⚡ Deep Work",
                            FocusCategory.STUDY to "📚 Study",
                            FocusCategory.MINDFULNESS to "🧘 Breathe",
                            FocusCategory.OFFLINE_HOBBY to "🎨 Offline"
                        ).forEach { (cat, label) ->
                            FilterChip(
                                selected = state.selectedCategory == cat,
                                onClick = { screenModel.handleIntent(FocusIntent.SelectCategory(cat)) },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .semantics {
                                        contentDescription = "$label focus category"
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Animated Circular Timer with Companion Canvas
                FocusTimerVisual(
                    state = state,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Large digital countdown
                Text(
                    text = state.formattedRemainingTime,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics {
                        contentDescription = "Remaining focus time: ${state.formattedRemainingTime}"
                    }
                )

                Text(
                    text = when (state.status) {
                        FocusTimerStatus.IDLE -> "Ready to breathe and focus together"
                        FocusTimerStatus.RUNNING -> "Companion is meditating peacefully with you"
                        FocusTimerStatus.PAUSED -> "Paused — take your time to resume"
                        FocusTimerStatus.COMPLETED -> "Session completed with warmth 🌱"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Timer Controls
                when (state.status) {
                    FocusTimerStatus.IDLE -> {
                        Button(
                            onClick = { screenModel.handleIntent(FocusIntent.StartTimer) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .semantics {
                                    contentDescription = "Start ${state.selectedDurationMinutes} minute mindful focus session"
                                }
                        ) {
                            Text("Begin Focus (${state.selectedDurationMinutes} min)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    FocusTimerStatus.RUNNING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { screenModel.handleIntent(FocusIntent.PauseTimer) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .semantics {
                                        contentDescription = "Pause focus session"
                                    }
                            ) {
                                Text("Pause")
                            }

                            Button(
                                onClick = { screenModel.handleIntent(FocusIntent.StopTimer) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .semantics {
                                        contentDescription = "End focus session gently"
                                    }
                            ) {
                                Text(
                                    "End Gently",
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    FocusTimerStatus.PAUSED -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { screenModel.handleIntent(FocusIntent.ResumeTimer) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .semantics {
                                        contentDescription = "Resume focus session"
                                    }
                            ) {
                                Text("Resume")
                            }

                            OutlinedButton(
                                onClick = { screenModel.handleIntent(FocusIntent.StopTimer) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .semantics {
                                        contentDescription = "Reset timer"
                                    }
                            ) {
                                Text("Reset")
                            }
                        }
                    }

                    FocusTimerStatus.COMPLETED -> {
                        Button(
                            onClick = { screenModel.handleIntent(FocusIntent.DismissCelebration) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .semantics {
                                    contentDescription = "Start new session"
                                }
                        ) {
                            Text("Start New Session")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Daily summary card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Today focus total: ${state.totalFocusMinutesToday} minutes completed"
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Mindful Focus",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${state.totalFocusMinutesToday} min",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "🌱 +${state.totalFocusMinutesToday * 2} XP earned",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Celebration dialog
        if (state.showCelebrationDialog) {
            AlertDialog(
                onDismissRequest = { screenModel.handleIntent(FocusIntent.DismissCelebration) },
                title = {
                    Text("🌱 Mindful Focus Complete!")
                },
                text = {
                    Column {
                        Text("You focused for ${state.selectedDurationMinutes} minutes alongside your companion.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+${state.earnedXp} XP (Wisdom & Energy) awarded!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Your companion feels cheerful and full of vitality.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { screenModel.handleIntent(FocusIntent.DismissCelebration) },
                        modifier = Modifier.semantics {
                            contentDescription = "Acknowledge celebration and return to timer"
                        }
                    ) {
                        Text("Wonderful")
                    }
                }
            )
        }
    }
}

@Composable
fun FocusTimerVisual(
    state: FocusState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FocusHaloAnimation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state.status == FocusTimerStatus.RUNNING) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloPulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val progress = state.progress

    Box(
        modifier = modifier
            .size(240.dp)
            .semantics {
                contentDescription = "Animated focus session timer with companion. Progress ${(progress * 100).toInt()}%"
            },
        contentAlignment = Alignment.Center
    ) {
        // Circular progress timer ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = size.minDimension - strokeWidth

            // Background track
            drawCircle(
                color = trackColor,
                radius = diameter / 2f,
                style = Stroke(width = strokeWidth)
            )

            // Active progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        primaryColor.copy(alpha = 0.7f),
                        primaryColor,
                        primaryColor.copy(alpha = 0.9f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Companion Canvas with meditation breathing
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CompanionCanvas(
                stage = state.companionStage,
                emotion = state.companionEmotion,
                modifier = Modifier.size(140.dp)
            )
        }
    }
}
