package org.awaremate.shared.presentation.growth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import org.awaremate.shared.domain.model.SelfDiscoveryPrompt

@Composable
fun SelfDiscoveryCard(
    prompt: SelfDiscoveryPrompt?,
    currentIndex: Int,
    totalCount: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onAcknowledge: (promptId: String, reflection: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (prompt == null) return

    var isAddingNote by remember(prompt.id) { mutableStateOf(false) }
    var noteText by remember(prompt.id) { mutableStateOf(prompt.userReflection ?: "") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Self-Discovery prompt card. Question: ${prompt.question}"
            }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Category and Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧭", fontSize = 20.sp)
                    Spacer(modifier = Modifier.padding(3.dp))
                    Text(
                        text = "Self-Discovery",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "${currentIndex + 1} of $totalCount",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Pill
            Text(
                text = prompt.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Main Question
            Text(
                text = prompt.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Curiosity Hint
            Text(
                text = "🌱 Curiosity Hint: ${prompt.curiosityHint}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // User Reflection Note section
            AnimatedVisibility(visible = isAddingNote) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("What did you notice about yourself? (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Reflection note for discovery prompt" },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (prompt.isAcknowledged && prompt.userReflection != null && !isAddingNote) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💭 Your observation: \"${prompt.userReflection}\"",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions: Navigation & Acknowledge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.semantics {
                            contentDescription = "Previous discovery prompt"
                        }
                    ) {
                        Text(text = "◀", fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.semantics {
                            contentDescription = "Next discovery prompt"
                        }
                    ) {
                        Text(text = "▶", fontSize = 16.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isAddingNote) {
                        OutlinedButton(
                            onClick = { isAddingNote = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Add reflection note to prompt"
                            }
                        ) {
                            Text("Note", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            onAcknowledge(prompt.id, noteText.takeIf { it.isNotBlank() })
                            isAddingNote = false
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Acknowledge noticing this habit pattern"
                        }
                    ) {
                        Text(if (prompt.isAcknowledged) "Noticed ✓" else "I noticed this! 💡", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
