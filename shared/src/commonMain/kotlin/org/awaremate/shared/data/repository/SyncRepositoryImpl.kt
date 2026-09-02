package org.awaremate.shared.data.repository

import org.awaremate.shared.data.local.dao.CompanionDao
import org.awaremate.shared.data.local.dao.DailyChallengeDao
import org.awaremate.shared.data.local.dao.FocusSessionDao
import org.awaremate.shared.data.local.dao.MoodEntryDao
import org.awaremate.shared.data.local.dao.UserDao
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.domain.repository.SyncRepository

class SyncRepositoryImpl(
    private val userDao: UserDao,
    private val companionDao: CompanionDao,
    private val moodEntryDao: MoodEntryDao,
    private val focusSessionDao: FocusSessionDao,
    private val dailyChallengeDao: DailyChallengeDao,
    private val cloudSyncService: CloudSyncService
) : SyncRepository {

    override suspend fun syncAll(): Result<Unit> = runCatching {
        // Sync user & companion
        userDao.getUserById("current")?.let { userEntity ->
            cloudSyncService.backupUser(userEntity.toDomain())
        }
        companionDao.getCompanionById("primary")?.let { companionEntity ->
            cloudSyncService.backupCompanion(companionEntity.toDomain())
        }
        // Sync pending items
        syncPendingItems().getOrThrow()
    }

    override suspend fun syncPendingItems(): Result<Unit> = runCatching {
        // Unsynced mood entries
        val unsyncedMoods = moodEntryDao.getUnsyncedEntries()
        for (mood in unsyncedMoods) {
            val result = cloudSyncService.backupMoodEntry(mood.toDomain())
            if (result.isSuccess) {
                moodEntryDao.markAsSynced(mood.id, true)
            }
        }

        // Unsynced focus sessions
        val unsyncedSessions = focusSessionDao.getUnsyncedSessions()
        for (session in unsyncedSessions) {
            val result = cloudSyncService.backupFocusSession(session.toDomain())
            if (result.isSuccess) {
                focusSessionDao.markAsSynced(session.id, true)
            }
        }

        // Unsynced daily challenges
        val unsyncedChallenges = dailyChallengeDao.getUnsyncedChallenges()
        for (challenge in unsyncedChallenges) {
            val result = cloudSyncService.backupDailyChallenge(challenge.toDomain())
            if (result.isSuccess) {
                dailyChallengeDao.markAsSynced(challenge.id, true)
            }
        }
    }
}
