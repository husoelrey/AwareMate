package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.awaremate.shared.data.local.dao.DailyChallengeDao
import org.awaremate.shared.data.local.entity.DailyChallengeEntity
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.repository.DailyChallengeRepository

class DailyChallengeRepositoryImpl(
    private val dailyChallengeDao: DailyChallengeDao,
    private val cloudSyncService: CloudSyncService? = null
) : DailyChallengeRepository {

    override fun getChallengesForDate(dateString: String): Flow<List<DailyChallenge>> {
        return dailyChallengeDao.getChallengesForDateFlow(dateString).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveChallenges(challenges: List<DailyChallenge>): Result<Unit> = runCatching {
        val entities = challenges.map { DailyChallengeEntity.fromDomain(it) }
        dailyChallengeDao.insertChallenges(entities)
        challenges.forEach { challenge ->
            cloudSyncService?.backupDailyChallenge(challenge)
        }
    }

    override suspend fun completeChallenge(id: String): Result<Unit> = runCatching {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        dailyChallengeDao.markAsCompleted(id, now)
    }
}
