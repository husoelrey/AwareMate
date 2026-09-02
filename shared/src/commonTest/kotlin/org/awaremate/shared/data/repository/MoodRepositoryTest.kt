package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.test.FakeCloudSyncService
import org.awaremate.shared.test.FakeMoodEntryDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoodRepositoryTest {

    @Test
    fun testInsertAndRetrieveMoodEntry() = runTest {
        val dao = FakeMoodEntryDao()
        val sync = FakeCloudSyncService()
        val repo = MoodRepositoryImpl(dao, sync)

        val entry = MoodEntry(
            id = "m-1",
            userId = "u-1",
            timestampEpochMs = 1000L,
            emoji = "🌿",
            moodScore = 4,
            energyLevel = 3,
            note = "Quiet morning",
            tags = listOf("peaceful")
        )

        repo.insertMoodEntry(entry).getOrThrow()

        val list = repo.getAllMoodEntries().first()
        assertEquals(1, list.size)
        assertEquals("m-1", list[0].id)
        assertEquals("🌿", list[0].emoji)
        assertTrue(list[0].isSynced)
        assertEquals(1, sync.backedUpMoods.size)
    }

    @Test
    fun testGetMoodEntriesForRange() = runTest {
        val dao = FakeMoodEntryDao()
        val repo = MoodRepositoryImpl(dao)

        val entry1 = MoodEntry(id = "m-1", userId = "u-1", timestampEpochMs = 100L, emoji = "🌱", moodScore = 3, energyLevel = 3)
        val entry2 = MoodEntry(id = "m-2", userId = "u-1", timestampEpochMs = 200L, emoji = "🌸", moodScore = 5, energyLevel = 4)
        val entry3 = MoodEntry(id = "m-3", userId = "u-1", timestampEpochMs = 500L, emoji = "☀️", moodScore = 4, energyLevel = 4)

        repo.insertMoodEntry(entry1)
        repo.insertMoodEntry(entry2)
        repo.insertMoodEntry(entry3)

        val inRange = repo.getMoodEntriesForRange(150L, 300L).first()
        assertEquals(1, inRange.size)
        assertEquals("m-2", inRange[0].id)
    }
}
