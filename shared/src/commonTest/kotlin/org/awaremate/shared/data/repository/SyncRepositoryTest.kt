package org.awaremate.shared.data.repository

import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.local.entity.DailyChallengeEntity
import org.awaremate.shared.data.local.entity.FocusSessionEntity
import org.awaremate.shared.data.local.entity.MoodEntryEntity
import org.awaremate.shared.test.FakeCloudSyncService
import org.awaremate.shared.test.FakeCompanionDao
import org.awaremate.shared.test.FakeDailyChallengeDao
import org.awaremate.shared.test.FakeFocusSessionDao
import org.awaremate.shared.test.FakeMoodEntryDao
import org.awaremate.shared.test.FakeUserDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncRepositoryTest {

    @Test
    fun testSyncPendingItems() = runTest {
        val userDao = FakeUserDao()
        val companionDao = FakeCompanionDao()
        val moodDao = FakeMoodEntryDao()
        val focusDao = FakeFocusSessionDao()
        val challengeDao = FakeDailyChallengeDao()
        val syncService = FakeCloudSyncService()

        val syncRepo = SyncRepositoryImpl(
            userDao = userDao,
            companionDao = companionDao,
            moodEntryDao = moodDao,
            focusSessionDao = focusDao,
            dailyChallengeDao = challengeDao,
            cloudSyncService = syncService
        )

        // Add unsynced items
        moodDao.insertMoodEntry(
            MoodEntryEntity(id = "m-unsynced", userId = "u-1", timestampEpochMs = 100L, emoji = "🌱", moodScore = 4, energyLevel = 3, isSynced = false)
        )
        focusDao.insertSession(
            FocusSessionEntity(id = "fs-unsynced", userId = "u-1", startTimeEpochMs = 200L, durationSeconds = 600, isSynced = false)
        )
        challengeDao.insertChallenge(
            DailyChallengeEntity(id = "dc-unsynced", userId = "u-1", title = "Walk", description = "Outside", dateString = "2026-09-02", isSynced = false)
        )

        assertEquals(1, moodDao.getUnsyncedEntries().size)
        assertEquals(1, focusDao.getUnsyncedSessions().size)
        assertEquals(1, challengeDao.getUnsyncedChallenges().size)

        // Perform sync
        val result = syncRepo.syncPendingItems()
        assertTrue(result.isSuccess)

        // Verify items backed up and marked as synced in local Room DAOs
        assertEquals(1, syncService.backedUpMoods.size)
        assertEquals(1, syncService.backedUpSessions.size)
        assertEquals(1, syncService.backedUpChallenges.size)

        assertTrue(moodDao.getUnsyncedEntries().isEmpty())
        assertTrue(focusDao.getUnsyncedSessions().isEmpty())
        assertTrue(challengeDao.getUnsyncedChallenges().isEmpty())
    }
}
