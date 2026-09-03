package org.awaremate.shared.presentation.growth.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCategory
import org.awaremate.shared.domain.model.HobbyEnergyLevel

@Composable
fun HobbyDiscoverySection(
    hobbies: List<Hobby>,
    selectedCategory: HobbyCategory?,
    onSelectCategory: (HobbyCategory?) -> Unit,
    onToggleBookmark: (hobbyId: String, isBookmarked: Boolean) -> Unit,
    onCompleteSession: (hobby: Hobby) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = "Offline Hobby Explorer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Screen-free pursuits tailored to your energy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text("All", fontSize = 12.sp) },
                modifier = Modifier.semantics {
                    contentDescription = "Show all hobby categories"
                }
            )

            HobbyCategory.entries.forEach { category ->
                val label = when (category) {
                    HobbyCategory.CREATIVE_ARTS -> "Creative"
                    HobbyCategory.NATURE_OUTDOORS -> "Nature"
                    HobbyCategory.MINDFUL_LIFESTYLE -> "Mindful"
                    HobbyCategory.HANDS_ON_CRAFT -> "Crafts"
                    HobbyCategory.MUSIC_LITERATURE -> "Music & Words"
                }
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onSelectCategory(if (selectedCategory == category) null else category) },
                    label = { Text(label, fontSize = 12.sp) },
                    modifier = Modifier.semantics {
                        contentDescription = "Filter hobbies by $label"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hobbies List
        if (hobbies.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No hobbies found for this category.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                hobbies.forEach { hobby ->
                    HobbyCard(
                        hobby = hobby,
                        onToggleBookmark = { onToggleBookmark(hobby.id, !hobby.isBookmarked) },
                        onCompleteSession = { onCompleteSession(hobby) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HobbyCard(
    hobby: Hobby,
    onToggleBookmark: () -> Unit,
    onCompleteSession: () -> Unit
) {
    val nowMs = Clock.System.now().toEpochMilliseconds()
    val isCompletedToday = hobby.lastCompletedEpochMs != null && (nowMs - hobby.lastCompletedEpochMs < 18 * 3600 * 1000L)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Hobby: ${hobby.title}. ${hobby.description}"
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hobby.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.semantics {
                        contentDescription = if (hobby.isBookmarked) "Remove bookmark" else "Bookmark hobby"
                    }
                ) {
                    Text(text = if (hobby.isBookmarked) "★" else "☆", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Metadata Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("${hobby.estimatedDurationMinutes}m", fontSize = 11.sp) }
                )
                val energyLabel = when (hobby.energyLevel) {
                    HobbyEnergyLevel.GENTLE -> "Gentle"
                    HobbyEnergyLevel.MODERATE -> "Moderate"
                    HobbyEnergyLevel.ACTIVE -> "Active"
                }
                SuggestionChip(
                    onClick = {},
                    label = { Text(energyLabel, fontSize = 11.sp) }
                )
                if (hobby.sessionsCompleted > 0) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Done ${hobby.sessionsCompleted}x", fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = hobby.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tip: ${hobby.beginnerTip}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isCompletedToday) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.semantics {
                            contentDescription = "Log completed offline session for ${hobby.title}"
                        }
                    ) {
                        Text("Logged Today ✓", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onCompleteSession,
                        modifier = Modifier.semantics {
                            contentDescription = "Log completed offline session for ${hobby.title}"
                        }
                    ) {
                        Text("Log Session (+25 XP)", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
