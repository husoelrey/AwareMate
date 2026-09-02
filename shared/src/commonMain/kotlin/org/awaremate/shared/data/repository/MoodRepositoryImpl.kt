package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.awaremate.shared.data.local.dao.MoodEntryDao
import org.awaremate.shared.data.local.entity.MoodEntryEntity
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.repository.MoodRepository

class MoodRepositoryImpl(
    private val moodEntryDao: MoodEntryDao,
    private val cloudSyncService: CloudSyncService? = null
) : MoodRepository {

    override fun getAllMoodEntries(): Flow<List<MoodEntry>> {
        return moodEntryDao.getAllMoodEntriesFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getMoodEntriesForRange(startEpochMs: Long, endEpochMs: Long): Flow<List<MoodEntry>> {
        return moodEntryDao.getMoodEntriesForRange(startEpochMs, endEpochMs).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertMoodEntry(entry: MoodEntry): Result<Unit> = runCatching {
        moodEntryDao.insertMoodEntry(MoodEntryEntity.fromDomain(entry.copy(isSynced = false)))
        val syncService = cloudSyncService
        if (syncService != null) {
            val syncResult = runCatching { syncService.backupMoodEntry(entry) }.getOrNull()
            if (syncResult?.isSuccess == true) {
                moodEntryDao.markAsSynced(entry.id, true)
            }
        }
    }

    override suspend fun deleteMoodEntry(id: String): Result<Unit> = runCatching {
        moodEntryDao.deleteMoodEntry(id)
    }
}
