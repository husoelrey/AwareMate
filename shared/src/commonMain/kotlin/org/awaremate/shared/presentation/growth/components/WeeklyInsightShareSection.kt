package org.awaremate.shared.presentation.growth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimeCorrelation
import org.awaremate.shared.presentation.analytics.MoodScreenTimeCorrelationChart

data class WeeklyMoodStripDay(
    val date: LocalDate,
    val dayLabel: String,
    val moodEntry: MoodEntry?
)

fun buildWeeklyMoodStrip(
    moodEntries: List<MoodEntry>,
    nowEpochMs: Long = Clock.System.now().toEpochMilliseconds(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): List<WeeklyMoodStripDay> {
    val today = Instant.fromEpochMilliseconds(nowEpochMs).toLocalDateTime(timeZone).date
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
    val latestMoodByDate = moodEntries
        .groupBy { Instant.fromEpochMilliseconds(it.timestampEpochMs).toLocalDateTime(timeZone).date }
        .mapValues { (_, entries) -> entries.maxBy { it.timestampEpochMs } }

    return (0..6).map { offset ->
        val date = monday.plus(offset, DateTimeUnit.DAY)
        WeeklyMoodStripDay(
            date = date,
            dayLabel = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
            moodEntry = latestMoodByDate[date]
        )
    }
}

@Composable
expect fun WeeklyInsightShareSection(
    moodEntries: List<MoodEntry>,
    correlation: WeeklyMoodScreenTimeCorrelation,
    modifier: Modifier = Modifier
)

@Composable
internal fun WeeklyInsightCardContent(
    moodEntries: List<MoodEntry>,
    correlation: WeeklyMoodScreenTimeCorrelation,
    modifier: Modifier = Modifier
) {
    val days = remember(moodEntries) { buildWeeklyMoodStrip(moodEntries) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Private weekly AwareMate insight card" },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(18.dp)
        ) {
            Text("My gentle week", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "A private AwareMate snapshot · ${days.first().date} – ${days.last().date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            WeeklyMoodStrip(days)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(14.dp))
            Text("Mood & screen-time rhythm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "A view of my own patterns—never a comparison.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (!correlation.hasEnoughMoodDays || correlation.points.size < 5) {
                Text(
                    "This part of the picture will appear as I add more check-ins.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("■ Screen time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("— Mood / energy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
                MoodScreenTimeCorrelationChart(data = correlation.points)
                correlation.observationalInsight?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("AwareMate · noticing, not judging", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun WeeklyMoodStrip(days: List<WeeklyMoodStripDay>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = days.joinToString(", ") { day ->
                    day.moodEntry?.let { "${day.dayLabel} ${it.emoji}" } ?: "${day.dayLabel} no check-in"
                }
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach { day ->
            val mood = day.moodEntry
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = mood?.let { moodEntryColor(it) }
                    ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(day.dayLabel, style = MaterialTheme.typography.labelSmall)
                    Text(mood?.emoji ?: "·", fontSize = 20.sp)
                    Text(day.date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
