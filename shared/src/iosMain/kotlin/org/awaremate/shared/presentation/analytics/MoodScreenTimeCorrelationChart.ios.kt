package org.awaremate.shared.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimePoint

@Composable
actual fun MoodScreenTimeCorrelationChart(data: List<WeeklyMoodScreenTimePoint>, modifier: Modifier) {
    val columnColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    val lineColor = MaterialTheme.colorScheme.tertiary
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .semantics { contentDescription = "Weekly mood, energy, and screen-time chart" }
    ) {
        if (data.isEmpty()) return@Canvas
        val maxScreenTime = data.maxOf { it.screenTimeMinutes }.coerceAtLeast(1)
        val slot = size.width / data.size
        val path = Path()
        data.forEachIndexed { index, point ->
            val barHeight = size.height * point.screenTimeMinutes / maxScreenTime
            val x = index * slot + slot * 0.2f
            drawRoundRect(
                color = columnColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(slot * 0.6f, barHeight),
                cornerRadius = CornerRadius(8f)
            )
            val lineX = index * slot + slot / 2f
            val lineY = size.height * (1f - (point.moodEnergyScore / 5.0).toFloat())
            if (index == 0) path.moveTo(lineX, lineY) else path.lineTo(lineX, lineY)
        }
        drawPath(path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
    }
}
