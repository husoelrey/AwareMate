package org.awaremate.shared.presentation.growth.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.awaremate.shared.domain.model.BreathingPattern
import org.awaremate.shared.domain.model.BreathingPhase
import org.awaremate.shared.domain.model.BreathingSessionState

@Composable
fun BreathingGuideDialog(
    state: BreathingSessionState,
    onStart: (pattern: BreathingPattern, cycles: Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPattern by remember { mutableStateOf(state.pattern) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Breath & Grounding",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Reconnect with your natural rhythm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pattern Selector Chips (only editable before session starts)
                if (!state.isActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BreathingPattern.entries.forEach { pattern ->
                            FilterChip(
                                selected = selectedPattern == pattern,
                                onClick = { selectedPattern = pattern },
                                label = { Text(text = pattern.title, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).semantics {
                                    contentDescription = "Select breathing pattern ${pattern.title}"
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedPattern.description,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Animated Radial Breathing Visualizer (Hardware-accelerated continuous curve)
                val phaseDurationMs = when (state.currentPhase) {
                    BreathingPhase.INHALE -> state.pattern.inhaleSeconds * 1000
                    BreathingPhase.HOLD_IN -> state.pattern.holdInSeconds * 1000
                    BreathingPhase.EXHALE -> state.pattern.exhaleSeconds * 1000
                    BreathingPhase.HOLD_OUT -> state.pattern.holdOutSeconds * 1000
                }

                val targetScale = when (state.currentPhase) {
                    BreathingPhase.INHALE -> 1.0f
                    BreathingPhase.HOLD_IN -> 1.0f
                    BreathingPhase.EXHALE -> 0.4f
                    BreathingPhase.HOLD_OUT -> 0.4f
                }

                val animatedScale by animateFloatAsState(
                    targetValue = if (state.isActive) targetScale else 0.45f,
                    animationSpec = tween(
                        durationMillis = if (state.isActive) phaseDurationMs.coerceAtLeast(300) else 400,
                        easing = FastOutSlowInEasing
                    )
                )

                val primaryColor = MaterialTheme.colorScheme.primary
                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .semantics {
                            contentDescription = if (state.isActive) {
                                "${state.currentPhase.instruction}, ${state.secondsRemainingInPhase} seconds remaining. Cycle ${state.currentCycle} of ${state.targetCycles}"
                            } else "Breathing visualizer idle"
                        }
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = size.minDimension / 2f - 12.dp.toPx()

                        // Outer subtle guide ring
                        drawCircle(
                            color = surfaceVariant.copy(alpha = 0.5f),
                            radius = maxRadius,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Ambient outer glow
                        val currentRadius = maxRadius * animatedScale.coerceIn(0.2f, 1.0f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    tertiaryColor.copy(alpha = 0.4f),
                                    primaryColor.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = currentRadius * 1.25f
                            ),
                            radius = currentRadius * 1.25f,
                            center = center
                        )

                        // Main breathing core circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.85f),
                                    tertiaryColor.copy(alpha = 0.7f)
                                ),
                                center = center,
                                radius = currentRadius
                            ),
                            radius = currentRadius,
                            center = center
                        )

                        // Pulsing border ring
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.9f),
                            radius = currentRadius,
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // Centered Instruction and Countdown Text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (state.isActive) {
                            Text(
                                text = state.currentPhase.instruction,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "${state.secondsRemainingInPhase}s",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Cycle ${state.currentCycle}/${state.targetCycles}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        } else if (state.isCompleted) {
                            Text(
                                text = "✨",
                                fontSize = 32.sp
                            )
                            Text(
                                text = "Grounded & Present",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "🌬️",
                                fontSize = 32.sp
                            )
                            Text(
                                text = "Ready",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Phase Cue or Completion Note
                if (state.isActive) {
                    Text(
                        text = state.currentPhase.cue,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (state.isCompleted) {
                    Text(
                        text = "Your breath is your anchor. Carry this calm throughout your day 🌱",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!state.isActive) {
                        Button(
                            onClick = { onStart(selectedPattern, 4) },
                            modifier = Modifier.fillMaxWidth().semantics {
                                contentDescription = "Start breathing session"
                            }
                        ) {
                            Text(if (state.isCompleted) "Breathe Again" else "Begin (4 Cycles)")
                        }
                    } else {
                        if (state.isPaused) {
                            Button(
                                onClick = onResume,
                                modifier = Modifier.weight(1f).semantics {
                                    contentDescription = "Resume breathing session"
                                }
                            ) {
                                Text("Resume")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onPause,
                                modifier = Modifier.weight(1f).semantics {
                                    contentDescription = "Pause breathing session"
                                }
                            ) {
                                Text("Pause")
                            }
                        }
                        Button(
                            onClick = onStop,
                            modifier = Modifier.weight(1f).semantics {
                                contentDescription = "End breathing session"
                            }
                        ) {
                            Text("End")
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    contentDescription = "Close breathing dialog"
                }
            ) {
                Text("Close")
            }
        },
        modifier = modifier.semantics {
            contentDescription = "Animated Breathing and Grounding Guide"
        }
    )
}
