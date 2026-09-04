package org.awaremate.shared.domain.usecase.growth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.data.repository.MoodRepositoryImpl
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase
import org.awaremate.shared.test.FakeCompanionDao
import org.awaremate.shared.test.FakeMoodEntryDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogMoodUseCaseTest {

    @Test
    fun testLogMoodPersistsEntryAwardsWisdomXpAndUpdatesEmotion() = runTest {
        val moodDao = FakeMoodEntryDao()
        val moodRepo = MoodRepositoryImpl(moodDao)
        val companionDao = FakeCompanionDao()
        val companionRepo = CompanionRepositoryImpl(companionDao)

        val addExpUseCase = AddExperienceUseCase(companionRepo)
        val momentumUseCase = UpdateMomentumUseCase(companionRepo)
        val emotionUseCase = UpdateCompanionEmotionUseCase(companionRepo)

        val logMoodUseCase = LogMoodUseCase(
            moodRepository = moodRepo,
            addExperienceUseCase = addExpUseCase,
            updateMomentumUseCase = momentumUseCase,
            updateCompanionEmotionUseCase = emotionUseCase
        )

        val entry = MoodEntry(
            id = "mood_test_1",
            userId = "primary",
            timestampEpochMs = 1725288000000L,
            emoji = "😊",
            moodScore = 4,
            energyLevel = 3,
            note = "Feeling grounded today",
            tags = listOf("Nature", "Calm")
        )

        val result = logMoodUseCase(entry)
        assertTrue(result.isSuccess)
        assertEquals(MoodLogOutcome.CREATED, result.getOrNull())

        // 1. Check entry in repository
        val stored = moodRepo.getAllMoodEntries().first()
        assertEquals(1, stored.size)
        assertEquals("mood_test_1", stored[0].id)
        assertEquals("😊", stored[0].emoji)
        assertEquals(4, stored[0].moodScore)

        // 2. Check companion received Wisdom XP
        val companion = companionRepo.getCompanion().first()
        assertEquals(15, companion?.wisdomXp)
        assertEquals(15, companion?.experiencePoints)

        // 3. Check companion emotion became PEACEFUL
        assertEquals(CompanionEmotion.PEACEFUL, companion?.emotion)

        val duplicateResult = logMoodUseCase(entry.copy(id = "second_attempt", emoji = "🌿"))
        assertEquals(MoodLogOutcome.ALREADY_LOGGED_TODAY, duplicateResult.getOrNull())
        assertEquals(1, moodRepo.getAllMoodEntries().first().size)
        assertEquals(15, companionRepo.getCompanion().first()?.wisdomXp)
    }
}
