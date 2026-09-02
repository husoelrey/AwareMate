package org.awaremate.shared.presentation.companion

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.presentation.theme.CreativityColor
import org.awaremate.shared.presentation.theme.EnergyColor
import org.awaremate.shared.presentation.theme.HappinessColor
import org.awaremate.shared.presentation.theme.WisdomColor

class CompanionScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<CompanionScreenModel>()
        val state by screenModel.state.collectAsState()

        var showRenameDialog by remember { mutableStateOf(false) }
        var tempName by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = state.companion.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                tempName = state.companion.name
                                showRenameDialog = true
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Rename companion button"
                            }
                        ) {
                            Text(text = "✏️", fontSize = 18.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Companion Detail Screen"
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
                            contentDescription = "Loading companion details"
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Interaction message banner
                    state.interactionMessage?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { screenModel.handleIntent(CompanionIntent.ClearInteractionMessage) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Dismiss interaction message"
                                    }
                                ) {
                                    Text("Dismiss", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // Canvas Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CompanionCanvas(
                            stage = state.companion.stage,
                            emotion = state.companion.emotion,
                            modifier = Modifier.fillMaxSize(),
                            onTap = {
                                screenModel.handleIntent(CompanionIntent.WaterCompanion(CompanionCategory.HAPPINESS))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Emotion and Stage Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stageName = state.companion.stage.name.lowercase().replaceFirstChar { it.uppercase() }
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Stage: $stageName") },
                            modifier = Modifier.semantics {
                                contentDescription = "Companion stage $stageName"
                            }
                        )

                        val emotionName = state.companion.emotion.name.lowercase().replaceFirstChar { it.uppercase() }
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Feeling: $emotionName") },
                            modifier = Modifier.semantics {
                                contentDescription = "Companion emotion $emotionName"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stage XP Progress Card
                    state.growthMetrics?.let { metrics ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Stage Progression",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${metrics.totalXp} XP Total",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { metrics.progressWithinStage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .semantics {
                                            contentDescription = "Growth stage progress ${(metrics.progressWithinStage * 100).toInt()}%"
                                        }
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (metrics.isMaxStage) {
                                        "Ancient Tree reached! Max evolution attained."
                                    } else {
                                        "${metrics.remainingXpForNextStage} XP needed to reach ${metrics.nextStage?.name?.lowercase()}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category XP Meters
                    Text(
                        text = "Category Growth",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .semantics { contentDescription = "Category Growth Section" }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CategoryXpRow(
                        category = "Happiness",
                        icon = "💛",
                        xp = state.companion.happinessXp,
                        color = HappinessColor
                    )
                    CategoryXpRow(
                        category = "Energy",
                        icon = "⚡",
                        xp = state.companion.energyXp,
                        color = EnergyColor
                    )
                    CategoryXpRow(
                        category = "Wisdom",
                        icon = "📘",
                        xp = state.companion.wisdomXp,
                        color = WisdomColor
                    )
                    CategoryXpRow(
                        category = "Creativity",
                        icon = "🎨",
                        xp = state.companion.creativityXp,
                        color = CreativityColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Interaction Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                screenModel.handleIntent(CompanionIntent.WaterCompanion(CompanionCategory.HAPPINESS))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "Water companion to grant love and 10 XP"
                                }
                        ) {
                            Text(text = "💧 Water Plant")
                        }

                        OutlinedButton(
                            onClick = {
                                screenModel.handleIntent(CompanionIntent.SetEmotion(CompanionEmotion.PEACEFUL))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "Meditate together with companion"
                                }
                        ) {
                            Text(text = "🧘 Meditate")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Rename Dialog
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Companion") },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("New Name") },
                        singleLine = true,
                        modifier = Modifier.semantics {
                            contentDescription = "New companion name input"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempName.isNotBlank()) {
                                screenModel.handleIntent(CompanionIntent.RenameCompanion(tempName.trim()))
                            }
                            showRenameDialog = false
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Save new companion name"
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRenameDialog = false },
                        modifier = Modifier.semantics {
                            contentDescription = "Cancel renaming companion"
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun CategoryXpRow(
    category: String,
    icon: String,
    xp: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .semantics {
                contentDescription = "$category XP: $xp points"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$xp XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
