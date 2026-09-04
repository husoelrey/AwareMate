package org.awaremate.shared.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.awaremate.shared.domain.model.MindfulNudgeCatalog
import org.awaremate.shared.domain.model.MissedCheckInReminderPolicy
import org.awaremate.shared.domain.repository.MoodRepository
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.domain.service.MissedCheckInReminderScheduler
import org.awaremate.shared.domain.service.NotificationService
import org.awaremate.shared.domain.usecase.sunset.DigitalSunsetUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MissedCheckInWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val moodRepository: MoodRepository by inject()
    private val preferencesRepository: PreferencesRepository by inject()
    private val notificationService: NotificationService by inject()

    override suspend fun doWork(): Result = runCatching {
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val localNow = now.toLocalDateTime(timeZone)
        val preferences = preferencesRepository.getPreferences().first()
        val hasMoodToday = moodRepository.getAllMoodEntries().first().any { entry ->
            Instant.fromEpochMilliseconds(entry.timestampEpochMs).toLocalDateTime(timeZone).date == localNow.date
        }
        val sunsetStatus = DigitalSunsetUseCase(preferencesRepository).getStatus(now.toEpochMilliseconds())

        if (MissedCheckInReminderPolicy.shouldNotify(preferences, localNow, hasMoodToday, sunsetStatus.stage)) {
            preferencesRepository.updatePreferences {
                it.copy(lastMissedCheckInNotificationDate = localNow.date.toString())
            }.getOrThrow()
            notificationService.showNudgeNotification(MindfulNudgeCatalog.missedCheckIn).getOrThrow()
        }

        if (preferences.notificationsEnabled && preferences.missedCheckInReminderEnabled) {
            AndroidMissedCheckInReminderScheduler.schedule(
                context = applicationContext,
                hour = preferences.missedCheckInReminderHour,
                minute = preferences.missedCheckInReminderMinute,
                forceTomorrow = true
            )
        }
        Result.success()
    }.getOrElse { Result.retry() }
}

class AndroidMissedCheckInReminderScheduler(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : MissedCheckInReminderScheduler {
    override suspend fun refresh() {
        val preferences = preferencesRepository.getPreferences().first()
        if (!preferences.notificationsEnabled || !preferences.missedCheckInReminderEnabled) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }
        schedule(
            context = context,
            hour = preferences.missedCheckInReminderHour,
            minute = preferences.missedCheckInReminderMinute,
            forceTomorrow = false
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "awaremate_missed_check_in"

        fun schedule(context: Context, hour: Int, minute: Int, forceTomorrow: Boolean) {
            val now = ZonedDateTime.now()
            var target = now.toLocalDate().atTime(hour, minute).atZone(now.zone)
            if (forceTomorrow || target.isBefore(now)) target = target.plusDays(1)
            val delayMillis = Duration.between(now, target).toMillis().coerceAtLeast(0)
            val request = OneTimeWorkRequestBuilder<MissedCheckInWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
