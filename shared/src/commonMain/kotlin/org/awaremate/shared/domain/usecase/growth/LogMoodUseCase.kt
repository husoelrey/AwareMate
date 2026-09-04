package org.awaremate.shared.domain.usecase.growth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEvent
import org.awaremate.shared.domain.model.MomentumCalculator
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.repository.MoodRepository
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase

enum class MoodLogOutcome {
    CREATED,
    ALREADY_LOGGED_TODAY
}

class LogMoodUseCase(
    private val moodRepository: MoodRepository,
    private val addExperienceUseCase: AddExperienceUseCase,
    private val updateMomentumUseCase: UpdateMomentumUseCase,
    private val updateCompanionEmotionUseCase: UpdateCompanionEmotionUseCase
) {
    private val logMutex = Mutex()

    companion object {
        const val MOOD_CHECKIN_XP = 15
    }

    suspend operator fun invoke(entry: MoodEntry): Result<MoodLogOutcome> = runCatching {
        logMutex.withLock {
            val timeZone = TimeZone.currentSystemDefault()
            val entryDate = Instant.fromEpochMilliseconds(entry.timestampEpochMs).toLocalDateTime(timeZone).date
            val alreadyLogged = moodRepository.getAllMoodEntries().first().any {
                Instant.fromEpochMilliseconds(it.timestampEpochMs).toLocalDateTime(timeZone).date == entryDate
            }
            if (alreadyLogged) return@withLock MoodLogOutcome.ALREADY_LOGGED_TODAY

            // 1. Insert mood entry into Room SSOT & trigger Firestore backup
            moodRepository.insertMoodEntry(entry).getOrThrow()

            // 2. Award Wisdom XP to companion
            addExperienceUseCase(
                category = CompanionCategory.WISDOM,
                amount = MOOD_CHECKIN_XP
            ).getOrThrow()

            // 3. Boost Momentum
            updateMomentumUseCase.boostMomentum(MomentumCalculator.BASE_ACTIVITY_BOOST).getOrThrow()

            // 4. Update companion emotion state (transitions to PEACEFUL)
            updateCompanionEmotionUseCase(CompanionEvent.MoodLogged(entry.moodScore)).getOrThrow()
            MoodLogOutcome.CREATED
        }
    }
}
