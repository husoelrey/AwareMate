package org.awaremate.shared.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.awaremate.shared.domain.model.DailyScreenTimeData

@Composable
actual fun ScreenTimeBarChart(
    data: List<DailyScreenTimeData>,
    dailyGoalMinutes: Int,
    modifier: Modifier
) {
    ScreenTimeCanvasBarChart(data = data, dailyGoalMinutes = dailyGoalMinutes, modifier = modifier)
}

@Composable
fun ScreenTimeCanvasBarChart(
    data: List<DailyScreenTimeData>,
    dailyGoalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val maxMinutes = (data.maxOfOrNull { it.screenTimeMinutes } ?: 180).coerceAtLeast(dailyGoalMinutes).coerceAtLeast(60)
    val primaryColor = MaterialTheme.colorScheme.primary
    val gentleCoralColor = MaterialTheme.colorScheme.error
    val goalColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .semantics {
                contentDescription = "Screen time weekly bar chart. 7 days breakdown."
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Screen Rhythm",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Goal: ${dailyGoalMinutes / 60}h ${dailyGoalMinutes % 60}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No screen time data recorded yet 🌱",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24f
                        val count = data.size.coerceAtLeast(1)
                        val barWidth = (canvasWidth / count) * 0.45f
                        val slotWidth = canvasWidth / count

                        val goalRatio = (dailyGoalMinutes.toFloat() / maxMinutes).coerceIn(0f, 1f)
                        val goalY = canvasHeight * (1f - goalRatio)
                        drawLine(
                            color = goalColor,
                            start = Offset(0f, goalY),
                            end = Offset(canvasWidth, goalY),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        data.forEachIndexed { index, item ->
                            val ratio = (item.screenTimeMinutes.toFloat() / maxMinutes).coerceIn(0f, 1f)
                            val barHeight = (canvasHeight * ratio).coerceAtLeast(4f)
                            val x = index * slotWidth + (slotWidth - barWidth) / 2f
                            val y = canvasHeight - barHeight

                            val barColor = if (item.screenTimeMinutes > dailyGoalMinutes && dailyGoalMinutes > 0) {
                                gentleCoralColor
                            } else {
                                primaryColor
                            }

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    data.forEach { item ->
                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
