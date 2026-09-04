package org.awaremate.shared.presentation.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimePoint

@Composable
actual fun MoodScreenTimeCorrelationChart(data: List<WeeklyMoodScreenTimePoint>, modifier: Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(data.map { it.screenTimeMinutes }) }
                lineSeries { series(data.map { it.moodEnergyScore }) }
            }
        }
    }

    Column(
        modifier = modifier.semantics {
            contentDescription = data.joinToString(
                prefix = "Weekly mood, energy, and screen-time chart. ",
                separator = ". "
            ) { "${it.dayLabel}: mood ${it.moodScore}, energy ${it.energyLevel}, screen time ${it.screenTimeMinutes} minutes" }
        }
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(verticalAxisPosition = Axis.Position.Vertical.Start),
                rememberLineCartesianLayer(verticalAxisPosition = Axis.Position.Vertical.End),
                startAxis = VerticalAxis.rememberStart(),
                endAxis = VerticalAxis.rememberEnd(),
                bottomAxis = HorizontalAxis.rememberBottom()
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth().height(180.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            data.forEach { point ->
                Text(
                    text = point.dayLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
