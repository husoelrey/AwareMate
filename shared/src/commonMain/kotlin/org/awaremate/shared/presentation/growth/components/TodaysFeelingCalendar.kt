package org.awaremate.shared.presentation.growth.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.SelfDiscoveryPrompt

private const val CALENDAR_PAGE_COUNT = 1201
private const val CALENDAR_INITIAL_PAGE = CALENDAR_PAGE_COUNT / 2

data class MoodCalendarDay(
    val date: LocalDate?,
    val moodEntry: MoodEntry?,
    val selfDiscoveryAnswers: List<SelfDiscoveryPrompt>
)

fun monthAtOffset(baseYear: Int, baseMonthNumber: Int, offset: Int): Pair<Int, Int> {
    val zeroBasedMonth = baseYear * 12 + (baseMonthNumber - 1) + offset
    val year = if (zeroBasedMonth >= 0) zeroBasedMonth / 12 else (zeroBasedMonth - 11) / 12
    val month = zeroBasedMonth - year * 12 + 1
    return year to month
}

fun buildMoodCalendarDays(
    year: Int,
    monthNumber: Int,
    moodEntries: List<MoodEntry>,
    prompts: List<SelfDiscoveryPrompt>,
    timeZone: TimeZone
): List<MoodCalendarDay> {
    val firstDay = LocalDate(year, monthNumber, 1)
    val leadingEmptyDays = firstDay.dayOfWeek.ordinal
    val daysInMonth = daysInMonth(year, monthNumber)
    val cellCount = ((leadingEmptyDays + daysInMonth + 6) / 7) * 7

    val moodsByDate = moodEntries
        .groupBy { Instant.fromEpochMilliseconds(it.timestampEpochMs).toLocalDateTime(timeZone).date }
        .mapValues { (_, entries) -> entries.maxBy { it.timestampEpochMs } }
    val promptsByDate = prompts
        .filter { it.userReflection != null && it.lastAnsweredEpochMs != null }
        .groupBy {
            Instant.fromEpochMilliseconds(requireNotNull(it.lastAnsweredEpochMs))
                .toLocalDateTime(timeZone).date
        }

    return List(cellCount) { index ->
        val dayNumber = index - leadingEmptyDays + 1
        val date = if (dayNumber in 1..daysInMonth) LocalDate(year, monthNumber, dayNumber) else null
        MoodCalendarDay(
            date = date,
            moodEntry = date?.let(moodsByDate::get),
            selfDiscoveryAnswers = date?.let { promptsByDate[it].orEmpty() }.orEmpty()
        )
    }
}

private fun daysInMonth(year: Int, monthNumber: Int): Int = when (monthNumber) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) 29 else 28
    else -> error("Month must be in 1..12")
}

private fun monthLabel(year: Int, monthNumber: Int): String {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return "${monthNames[monthNumber - 1]} $year"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodaysFeelingCalendar(
    moodEntries: List<MoodEntry>,
    selfDiscoveryPrompts: List<SelfDiscoveryPrompt>,
    modifier: Modifier = Modifier
) {
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.todayIn(timeZone) }
    val pagerState = rememberPagerState(
        initialPage = CALENDAR_INITIAL_PAGE,
        pageCount = { CALENDAR_PAGE_COUNT }
    )
    var selectedDay by remember { mutableStateOf<MoodCalendarDay?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Today's Feeling monthly calendar" },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(vertical = 18.dp)) {
            Text(
                text = "Today's Feeling",
                modifier = Modifier.padding(horizontal = 18.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Swipe through months and tap any day to revisit it.",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { page ->
                val (year, month) = monthAtOffset(today.year, today.monthNumber, page - CALENDAR_INITIAL_PAGE)
                val days = buildMoodCalendarDays(year, month, moodEntries, selfDiscoveryPrompts, timeZone)
                val rowCount = days.size / 7

                Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Text(
                        text = monthLabel(year, month),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((rowCount * 52).dp),
                        userScrollEnabled = false
                    ) {
                        items(days) { day ->
                            MoodDayCell(day = day, onClick = { selectedDay = day })
                        }
                    }
                }
            }
        }
    }

    selectedDay?.date?.let {
        MoodDayDetailsDialog(day = requireNotNull(selectedDay), onDismiss = { selectedDay = null })
    }
}

@Composable
private fun MoodDayCell(day: MoodCalendarDay, onClick: () -> Unit) {
    val date = day.date
    if (date == null) {
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }

    val mood = day.moodEntry
    val background = if (mood == null) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    } else {
        moodColor(mood)
    }
    val description = if (mood == null) {
        "$date, no check-in saved"
    } else {
        "$date, ${mood.emoji}, mood ${mood.moodScore} of 5, energy ${mood.energyLevel} of 5"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .semantics {
                role = Role.Button
                contentDescription = description
            },
        shape = RoundedCornerShape(12.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = mood?.emoji ?: date.dayOfMonth.toString(), fontSize = if (mood == null) 12.sp else 16.sp)
            if (mood != null) {
                Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    repeat(mood.energyLevel.coerceIn(1, 5)) {
                        Box(
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun moodColor(mood: MoodEntry): Color = when (mood.moodScore.coerceIn(1, 5)) {
    1 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f + mood.energyLevel * 0.05f)
    2 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f + mood.energyLevel * 0.05f)
    3 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f + mood.energyLevel * 0.05f)
    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f + mood.energyLevel * 0.05f)
}

@Composable
private fun MoodDayDetailsDialog(day: MoodCalendarDay, onDismiss: () -> Unit) {
    val date = requireNotNull(day.date)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.toString()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val mood = day.moodEntry
                if (mood == null) {
                    Text("No check-in was saved for this day. Blank days are simply part of your story.")
                } else {
                    Text("${mood.emoji}  Mood ${mood.moodScore}/5 · Energy ${mood.energyLevel}/5")
                    Text(mood.note?.takeIf { it.isNotBlank() } ?: "No note added.")
                }

                day.selfDiscoveryAnswers.forEach { prompt ->
                    Text(
                        text = "Self-Discovery",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(prompt.question, style = MaterialTheme.typography.bodySmall)
                    Text(prompt.userReflection.orEmpty())
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
