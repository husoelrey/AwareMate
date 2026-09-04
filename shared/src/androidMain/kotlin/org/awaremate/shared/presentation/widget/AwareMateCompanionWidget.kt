package org.awaremate.shared.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.toVisualState
import org.awaremate.shared.domain.repository.CompanionRepository
import org.awaremate.shared.domain.repository.MoodRepository
import org.awaremate.shared.domain.usecase.growth.LogMoodUseCase
import org.awaremate.shared.presentation.growth.components.DEFAULT_MOOD_CHECK_IN_ENERGY_LEVEL
import org.awaremate.shared.presentation.growth.components.moodOptions
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val moodEmojiKey = ActionParameters.Key<String>("mood_emoji")

class AwareMateCompanionWidget : GlanceAppWidget(), KoinComponent {
    private val companionRepository: CompanionRepository by inject()
    private val moodRepository: MoodRepository by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (companion, todayMood) = withContext(Dispatchers.Default) {
            val companion = companionRepository.getCompanion().first() ?: Companion()
            val timeZone = TimeZone.currentSystemDefault()
            val today = Clock.System.now().toLocalDateTime(timeZone).date
            val mood = moodRepository.getAllMoodEntries().first().firstOrNull {
                Instant.fromEpochMilliseconds(it.timestampEpochMs).toLocalDateTime(timeZone).date == today
            }
            companion to mood
        }
        provideContent { CompanionWidgetContent(companion, todayMood) }
    }
}

class AwareMateCompanionWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AwareMateCompanionWidget()
}

class LogMoodFromWidgetAction : ActionCallback, KoinComponent {
    private val moodRepository: MoodRepository by inject()
    private val logMoodUseCase: LogMoodUseCase by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(timeZone).date
        val alreadyLogged = moodRepository.getAllMoodEntries().first().any {
            Instant.fromEpochMilliseconds(it.timestampEpochMs).toLocalDateTime(timeZone).date == today
        }
        if (!alreadyLogged) {
            val emoji = parameters[moodEmojiKey] ?: return
            val option = moodOptions.firstOrNull { it.emoji == emoji } ?: return
            logMoodUseCase(
                MoodEntry(
                    id = "mood_${now.toEpochMilliseconds()}",
                    userId = "primary",
                    timestampEpochMs = now.toEpochMilliseconds(),
                    emoji = option.emoji,
                    moodScore = option.score,
                    energyLevel = DEFAULT_MOOD_CHECK_IN_ENERGY_LEVEL
                )
            )
        }
        AwareMateCompanionWidget().updateAll(context)
    }
}

@Composable
private fun CompanionWidgetContent(companion: Companion, todayMood: MoodEntry?) {
    val visual = companion.toVisualState()
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF2F7EF))
            .cornerRadius(20.dp)
            .padding(16.dp)
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = "${visual.stageGlyph} ${visual.emotionGlyph}",
                style = TextStyle(fontSize = 28.sp)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Column {
                Text(
                    text = companion.name,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF234F27)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${visual.stageLabel} · ${visual.emotionLabel}",
                    style = TextStyle(color = ColorProvider(Color(0xFF49634B)), fontSize = 12.sp)
                )
            }
        }
        Spacer(modifier = GlanceModifier.height(12.dp))
        if (todayMood != null) {
            Text(
                text = "Today: ${todayMood.emoji}  Energy ${todayMood.energyLevel}/5",
                style = TextStyle(color = ColorProvider(Color(0xFF234F27)), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                text = "Your check-in is safely recorded.",
                style = TextStyle(color = ColorProvider(Color(0xFF49634B)), fontSize = 12.sp)
            )
        } else {
            Text(
                text = "How is today feeling?",
                style = TextStyle(color = ColorProvider(Color(0xFF234F27)), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                moodOptions.forEach { option ->
                    Text(
                        text = option.emoji,
                        modifier = GlanceModifier
                            .defaultWeight()
                            .height(48.dp)
                            .clickable(
                                actionRunCallback<LogMoodFromWidgetAction>(
                                    actionParametersOf(moodEmojiKey to option.emoji)
                                )
                            ),
                        style = TextStyle(fontSize = 24.sp)
                    )
                }
            }
        }
    }
}

actual suspend fun updateCompanionCheckInWidget() {
    val context = org.awaremate.shared.AppContextProvider.appContext ?: return
    AwareMateCompanionWidget().updateAll(context)
}
