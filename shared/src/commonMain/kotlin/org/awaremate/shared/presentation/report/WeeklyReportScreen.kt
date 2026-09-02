package org.awaremate.shared.presentation.report

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import cafe.adriel.voyager.core.screen.Screen
import org.awaremate.shared.domain.model.DailyScreenTimeData
import org.awaremate.shared.domain.model.WeeklyDigitalAwarenessReport
import org.awaremate.shared.presentation.analytics.AppUsageRow
import org.awaremate.shared.presentation.analytics.ScreenTimeBarChart

class WeeklyReportScreen(
    private val report: WeeklyDigitalAwarenessReport,
    private val dailyGoalMinutes: Int = 180
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val chartData = report.dailySummaries.mapIndexed { index, summary ->
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            DailyScreenTimeData(
                dayLabel = if (index in days.indices) days[index] else "D$index",
                dateString = summary.dateString,
                screenTimeMinutes = summary.totalMinutes,
                goalMinutes = dailyGoalMinutes
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Weekly Awareness Report",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${report.startDate} to ${report.endDate}",
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
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Weekly Digital Awareness Report Screen"
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

                // Compassionate Reflection Card
                item {
                    ReflectionCard(insight = report.compassionateInsight, momentum = report.momentumTrend)
                }

                // 2x2 Highlights Grid
                item {
                    ReportHighlightsGrid(report = report)
                }

                // Weekly 7-day Bar Chart
                item {
                    ScreenTimeBarChart(
                        data = chartData,
                        dailyGoalMinutes = dailyGoalMinutes
                    )
                }

                // Top Apps Header
                item {
                    Text(
                        text = "Most Visited Spaces This Week",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Top Apps list
                items(report.topApps) { app ->
                    AppUsageRow(app = app, totalDayMinutes = report.totalScreenTimeMinutes)
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun ReflectionCard(insight: String, momentum: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Weekly companion reflection: $insight"
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌱 Companion's Weekly Reflection",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = momentum,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ReportHighlightsGrid(report: WeeklyDigitalAwarenessReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HighlightMiniCard(
            title = "Daily Average",
            value = "${report.dailyAverageMinutes / 60}h ${report.dailyAverageMinutes % 60}m",
            subtitle = "${report.totalScreenTimeMinutes / 60}h total screen time",
            icon = "📱",
            modifier = Modifier.weight(1f)
        )

        HighlightMiniCard(
            title = "Mindful Focus",
            value = "${report.totalFocusMinutes} min",
            subtitle = "${report.focusSessionsCount} sessions completed",
            icon = "🎯",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HighlightMiniCard(
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = modifier.semantics {
            contentDescription = "$title: $value, $subtitle"
        }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
