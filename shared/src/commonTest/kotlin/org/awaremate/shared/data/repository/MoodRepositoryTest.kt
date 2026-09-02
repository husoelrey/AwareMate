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

    @Test
    fun testOfflineInsertAndSubsequentSyncNoDataLoss() = runTest {
        val dao = FakeMoodEntryDao()

        // 1. Simulate Airplane Mode: CloudSyncService fails with NetworkException
        var isOnline = false
        val failingSyncService = object : org.awaremate.shared.data.remote.CloudSyncService {
            val uploadedMoods = mutableListOf<MoodEntry>()

            override suspend fun backupUser(user: org.awaremate.shared.domain.model.User) = Result.success(Unit)
            override suspend fun backupCompanion(companion: org.awaremate.shared.domain.model.Companion) = Result.success(Unit)
            override suspend fun backupFocusSession(session: org.awaremate.shared.domain.model.FocusSession) = Result.success(Unit)
            override suspend fun backupDailyChallenge(challenge: org.awaremate.shared.domain.model.DailyChallenge) = Result.success(Unit)
            override suspend fun fetchCloudCompanion(userId: String) = Result.success(null)
            override suspend fun fetchCloudMoodEntries(userId: String) = Result.success(emptyList<MoodEntry>())

            override suspend fun backupMoodEntry(entry: MoodEntry): Result<Unit> {
                return if (isOnline) {
                    uploadedMoods.add(entry)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Airplane mode: No network connectivity"))
                }
            }
        }

        val repo = MoodRepositoryImpl(dao, failingSyncService)
        val offlineEntry = MoodEntry(
            id = "offline_mood_1",
            userId = "u-1",
            timestampEpochMs = 1725289000000L,
            emoji = "🥱",
            moodScore = 2,
            energyLevel = 2,
            note = "Logged in airplane mode",
            tags = listOf("Offline", "Quiet")
        )

        // 2. User logs mood while offline: MUST SUCCEED LOCALLY (no data loss)
        val insertResult = repo.insertMoodEntry(offlineEntry)
        assertTrue(insertResult.isSuccess, "Offline insert must succeed locally")

        val localMoods = repo.getAllMoodEntries().first()
        assertEquals(1, localMoods.size)
        assertEquals("offline_mood_1", localMoods[0].id)
        assertEquals(false, localMoods[0].isSynced, "Entry must be flagged as unsynced in Room")
        assertEquals(0, failingSyncService.uploadedMoods.size, "Nothing should have reached cloud yet")

        // 3. User reconnects to internet: SyncRepository processes pending unsynced items
        isOnline = true
        val syncRepo = SyncRepositoryImpl(
            userDao = org.awaremate.shared.test.FakeUserDao(),
            companionDao = org.awaremate.shared.test.FakeCompanionDao(),
            moodEntryDao = dao,
            focusSessionDao = org.awaremate.shared.test.FakeFocusSessionDao(),
            dailyChallengeDao = org.awaremate.shared.test.FakeDailyChallengeDao(),
            cloudSyncService = failingSyncService
        )

        val syncResult = syncRepo.syncPendingItems()
        assertTrue(syncResult.isSuccess)

        // 4. Verify cloud received the entry and Room updated isSynced = true
        assertEquals(1, failingSyncService.uploadedMoods.size)
        assertEquals("offline_mood_1", failingSyncService.uploadedMoods[0].id)

        val updatedLocalMoods = repo.getAllMoodEntries().first()
        assertEquals(true, updatedLocalMoods[0].isSynced, "Room entry must now be marked as synced")
    }
}
