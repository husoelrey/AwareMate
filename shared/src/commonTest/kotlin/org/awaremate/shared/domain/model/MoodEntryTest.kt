package org.awaremate.shared.domain.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MoodEntryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testMoodEntrySerialization() {
        val entry = MoodEntry(
            id = "mood-123",
            userId = "user-abc",
            timestampEpochMs = 1725280000000L,
            emoji = "🌿",
            moodScore = 4,
            energyLevel = 3,
            note = "Had a peaceful walk in the park",
            tags = listOf("nature", "mindful", "calm"),
            isSynced = false
        )

        val encoded = json.encodeToString(entry)
        val decoded = json.decodeFromString<MoodEntry>(encoded)

        assertEquals("mood-123", decoded.id)
        assertEquals("user-abc", decoded.userId)
        assertEquals("🌿", decoded.emoji)
        assertEquals(4, decoded.moodScore)
        assertEquals(3, decoded.energyLevel)
        assertEquals("Had a peaceful walk in the park", decoded.note)
        assertEquals(listOf("nature", "mindful", "calm"), decoded.tags)
        assertFalse(decoded.isSynced)
    }
}
