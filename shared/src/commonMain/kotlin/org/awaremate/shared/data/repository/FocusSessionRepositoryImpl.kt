package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.awaremate.shared.data.local.dao.FocusSessionDao
import org.awaremate.shared.data.local.entity.FocusSessionEntity
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.domain.model.FocusSession
import org.awaremate.shared.domain.repository.FocusSessionRepository

class FocusSessionRepositoryImpl(
    private val focusSessionDao: FocusSessionDao,
    private val cloudSyncService: CloudSyncService? = null
) : FocusSessionRepository {

    override fun getRecentSessions(limit: Int): Flow<List<FocusSession>> {
        return focusSessionDao.getRecentSessionsFlow(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveSession(session: FocusSession): Result<Unit> = runCatching {
        focusSessionDao.insertSession(FocusSessionEntity.fromDomain(session.copy(isSynced = false)))
        if (cloudSyncService != null) {
            val syncResult = cloudSyncService.backupFocusSession(session)
            if (syncResult.isSuccess) {
                focusSessionDao.markAsSynced(session.id, true)
            }
        }
    }

    override suspend fun getTotalFocusMinutes(): Long {
        val totalSeconds = focusSessionDao.getTotalFocusSeconds() ?: 0L
        return totalSeconds / 60
    }
}
