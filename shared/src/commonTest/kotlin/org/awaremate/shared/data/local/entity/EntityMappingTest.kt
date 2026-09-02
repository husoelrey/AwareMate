package org.awaremate.shared.data.local.entity

import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.FocusCategory
import org.awaremate.shared.domain.model.FocusSession
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntityMappingTest {

    @Test
    fun testUserEntityMapping() {
        val user = User(
            id = "u-1",
            displayName = "Alex",
            email = "alex@awaremate.org",
            isAnonymous = false,
            createdAtEpochMs = 1000L,
            lastActiveEpochMs = 2000L
        )

        val entity = UserEntity.fromDomain(user)
        val mapped = entity.toDomain()

        assertEquals(user, mapped)
    }

    @Test
    fun testCompanionEntityMapping() {
        val companion = Companion(
            id = "primary",
            name = "Oakley",
            stage = CompanionStage.BLOOM,
            emotion = CompanionEmotion.CHEERFUL,
            experiencePoints = 450,
            momentumScore = 75.0,
            happinessXp = 100,
            energyXp = 150,
            wisdomXp = 100,
            creativityXp = 100,
            lastUpdatedEpochMs = 5000L
        )

        val entity = CompanionEntity.fromDomain(companion)
        val mapped = entity.toDomain()

        assertEquals(companion, mapped)
    }

    @Test
    fun testMoodEntryEntityMapping() {
        val entry = MoodEntry(
            id = "m-1",
            userId = "u-1",
            timestampEpochMs = 123456789L,
            emoji = "🌸",
            moodScore = 5,
            energyLevel = 4,
            note = "Feeling great!",
            tags = listOf("happy", "grateful"),
            isSynced = true
        )

        val entity = MoodEntryEntity.fromDomain(entry)
        val mapped = entity.toDomain()

        assertEquals(entry, mapped)
    }

    @Test
    fun testFocusSessionEntityMapping() {
        val session = FocusSession(
            id = "fs-1",
            userId = "u-1",
            startTimeEpochMs = 99999L,
            durationSeconds = 1500,
            category = FocusCategory.STUDY,
            earnedXp = 25,
            completed = true,
            note = "Math study",
            isSynced = false
        )

        val entity = FocusSessionEntity.fromDomain(session)
        val mapped = entity.toDomain()

        assertEquals(session, mapped)
    }

    @Test
    fun testDailyChallengeEntityMapping() {
        val challenge = DailyChallenge(
            id = "dc-1",
            userId = "u-1",
            title = "Walk outside",
            description = "15 min walk",
            xpReward = 20,
            dateString = "2026-09-02",
            completed = true,
            completedAtEpochMs = 8888L,
            isSynced = true
        )

        val entity = DailyChallengeEntity.fromDomain(challenge)
        val mapped = entity.toDomain()

        assertEquals(challenge, mapped)
    }

    @Test
    fun testRoomTypeConverters() {
        val converters = RoomTypeConverters()
        val originalList = listOf("tag1", "tag2", "tag3")
        val jsonString = converters.fromStringList(originalList)
        val restoredList = converters.toStringList(jsonString)

        assertEquals(originalList, restoredList)
        assertTrue(converters.toStringList(null).isEmpty())
        assertTrue(converters.toStringList("").isEmpty())
    }
}
