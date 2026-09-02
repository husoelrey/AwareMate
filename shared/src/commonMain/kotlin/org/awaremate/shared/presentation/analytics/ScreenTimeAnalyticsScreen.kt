package org.awaremate.shared.presentation.analytics

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import org.awaremate.shared.domain.model.AppCategory
import org.awaremate.shared.domain.model.AppUsageInfo
import org.awaremate.shared.domain.model.DailyScreenTimeData
import org.awaremate.shared.domain.model.DailyUsageSummary

data class ScreenTimeAnalyticsState(
    val hasPermission: Boolean = false,
    val todayUsage: DailyUsageSummary = DailyUsageSummary("Today", 0L),
    val dailyGoalMinutes: Int = 180,
    val weeklyChartData: List<DailyScreenTimeData> = emptyList()
)

class ScreenTimeAnalyticsScreen(
    private val staticState: ScreenTimeAnalyticsState? = null,
    private val onGrantPermissionClick: (() -> Unit)? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val (state, grantClick) = if (staticState != null) {
            staticState to (onGrantPermissionClick ?: {})
        } else {
            val screenModel = koinScreenModel<ScreenTimeAnalyticsScreenModel>()
            val dynamicState by screenModel.state.collectAsState()
            dynamicState to (onGrantPermissionClick ?: { screenModel.requestUsagePermission() })
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Screen Time Analytics",
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
                    contentDescription = "Screen Time Analytics Screen"
                }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Permission Guidance Banner (if access not granted)
                if (!state.hasPermission) {
                    item {
                        UsagePermissionCard(onGrantPermissionClick = grantClick)
                    }
                }

                // Daily Overview Card
                item {
                    DailyOverviewCard(
                        todayMinutes = state.todayUsage.totalMinutes,
                        goalMinutes = state.dailyGoalMinutes,
                        pickups = state.todayUsage.pickupsCount
                    )
                }

                // Vico 7-day Bar Chart
                item {
                    ScreenTimeBarChart(
                        data = state.weeklyChartData,
                        dailyGoalMinutes = state.dailyGoalMinutes
                    )
                }

                // Top Apps Header
                item {
                    Text(
                        text = "Today's Digital Rhythm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Top Apps Breakdown
                items(state.todayUsage.appUsages) { app ->
                    AppUsageRow(app = app, totalDayMinutes = state.todayUsage.totalMinutes)
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun UsagePermissionCard(onGrantPermissionClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Usage access permission guidance card"
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌱 Unlock Automatic Screen Time Tracking",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "AwareMate calculates screen time 100% on your device. Your data never leaves your phone or connects to third-party ad networks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onGrantPermissionClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.semantics {
                    contentDescription = "Grant usage access permission button"
                }
            ) {
                Text("Grant Usage Access")
            }
        }
    }
}

@Composable
fun DailyOverviewCard(todayMinutes: Int, goalMinutes: Int, pickups: Int) {
    val progress = if (goalMinutes > 0) (todayMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f) else 0f
    val hours = todayMinutes / 60
    val mins = todayMinutes % 60

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Today screen time overview: $hours hours $mins minutes out of $goalMinutes minutes goal"
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
                        text = "Today's Screen Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$pickups", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(text = "pickups", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (todayMinutes > goalMinutes && goalMinutes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (todayMinutes <= goalMinutes) {
                    "🌱 ${(goalMinutes - todayMinutes) / 60}h ${(goalMinutes - todayMinutes) % 60}m remaining in your daily intention"
                } else {
                    "🌿 Daily intention reached. A gentle reminder to rest your eyes whenever ready."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppUsageRow(app: AppUsageInfo, totalDayMinutes: Int) {
    val percentage = if (totalDayMinutes > 0) ((app.totalMinutes.toFloat() / totalDayMinutes) * 100).toInt() else 0
    val icon = when (app.category) {
        AppCategory.EDUCATION -> "📚"
        AppCategory.PRODUCTIVITY -> "⚡"
        AppCategory.SOCIAL_COMMUNICATION -> "💬"
        AppCategory.ENTERTAINMENT_GAMES -> "🎮"
        AppCategory.CREATIVITY_TOOLS -> "🎨"
        AppCategory.HEALTH_WELLNESS -> "🌱"
        AppCategory.OTHER -> "📱"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .semantics {
                contentDescription = "${app.appName}: ${app.formattedTime}, $percentage percent of total"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
