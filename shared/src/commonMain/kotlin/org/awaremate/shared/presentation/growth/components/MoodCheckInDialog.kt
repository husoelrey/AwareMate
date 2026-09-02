package org.awaremate.shared.presentation.growth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class MoodOption(
    val emoji: String,
    val score: Int,
    val label: String
)

private val moodOptions = listOf(
    MoodOption("😄", 5, "Joyful"),
    MoodOption("😊", 4, "Content"),
    MoodOption("🌿", 3, "Steady"),
    MoodOption("🥱", 2, "Tired"),
    MoodOption("🌧️", 1, "Tender")
)

private val availableTags = listOf(
    "🌿 Nature", "💤 Good Sleep", "📚 Study/Work",
    "🤝 Connected", "📱 Screen-Free", "🎨 Creative",
    "☕ Quiet Time", "🏃 Movement"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodCheckInDialog(
    onDismiss: () -> Unit,
    onSubmit: (emoji: String, moodScore: Int, energyLevel: Int, note: String?, tags: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf(moodOptions[1]) }
    var energyLevel by remember { mutableFloatStateOf(3f) }
    var noteText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Daily Mood Check-in",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "How is your internal weather today?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Emoji Selection
                Text(
                    text = "Emotional Climate",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moodOptions.forEach { option ->
                        val isSelected = selectedMood == option
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .clickable { selectedMood = option }
                                .semantics {
                                    contentDescription = "Mood ${option.label}, score ${option.score}"
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = option.emoji, fontSize = 26.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Energy Level Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Energy Battery",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when (energyLevel.toInt()) {
                            1 -> "Low / Recharging 🔋"
                            2 -> "Gentle Pace 🪫"
                            3 -> "Balanced ⚡"
                            4 -> "Lively & Fresh ⚡⚡"
                            else -> "Vibrant Energy 🌟"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = energyLevel,
                    onValueChange = { energyLevel = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.semantics {
                        contentDescription = "Energy level slider. Current level: ${energyLevel.toInt()}"
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Optional Reflection Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Reflection Note (Optional)") },
                    placeholder = { Text("What made you feel this way? No pressure...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Optional reflection note text field"
                        },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Quick Context Tags
                Text(
                    text = "What was present today?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            },
                            label = { Text(text = tag, fontSize = 12.sp) },
                            modifier = Modifier.semantics {
                                contentDescription = "Tag $tag, selected: $isSelected"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        selectedMood.emoji,
                        selectedMood.score,
                        energyLevel.toInt(),
                        noteText.takeIf { it.isNotBlank() },
                        selectedTags.toList()
                    )
                },
                modifier = Modifier.semantics {
                    contentDescription = "Save mood check-in"
                }
            ) {
                Text("Save Check-in")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    contentDescription = "Cancel mood check-in"
                }
            ) {
                Text("Cancel")
            }
        },
        modifier = modifier.semantics {
            contentDescription = "Mood Check-in Dialog"
        }
    )
}
