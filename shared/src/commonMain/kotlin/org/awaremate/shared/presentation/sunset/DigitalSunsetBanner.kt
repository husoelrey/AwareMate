package org.awaremate.shared.presentation.sunset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.awaremate.shared.domain.usecase.sunset.SunsetStage
import org.awaremate.shared.domain.usecase.sunset.SunsetStatus

@Composable
fun DigitalSunsetBanner(
    status: SunsetStatus,
    modifier: Modifier = Modifier
) {
    if (status.stage == SunsetStage.DAYTIME) return

    val sunsetGradient = Brush.horizontalGradient(
        listOf(
            Color(0xFFE07A5F).copy(alpha = 0.85f),
            Color(0xFFF2CC8F).copy(alpha = 0.85f)
        )
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(sunsetGradient)
            .semantics {
                contentDescription = "Digital Sunset banner: ${status.message}"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (status.stage) {
                        SunsetStage.SUNSET_APPROACHING -> "🌅"
                        SunsetStage.SUNSET_ACTIVE -> "🌆"
                        SunsetStage.BEDTIME -> "🌙"
                        else -> "☀️"
                    },
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (status.stage) {
                        SunsetStage.SUNSET_APPROACHING -> "Sunset Approaching (${status.minutesUntilSunset}m)"
                        SunsetStage.SUNSET_ACTIVE -> "Digital Sunset Active"
                        SunsetStage.BEDTIME -> "Bedtime Wind-Down"
                        else -> "Digital Rhythm"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = status.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}
