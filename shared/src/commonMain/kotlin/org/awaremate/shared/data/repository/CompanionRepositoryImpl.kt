package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.awaremate.shared.data.local.dao.CompanionDao
import org.awaremate.shared.data.local.entity.CompanionEntity
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionGrowthRules
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.model.MomentumCalculator
import org.awaremate.shared.domain.repository.CompanionRepository

class CompanionRepositoryImpl(
    private val companionDao: CompanionDao,
    private val cloudSyncService: CloudSyncService? = null
) : CompanionRepository {

    override fun getCompanion(): Flow<Companion?> {
        return companionDao.getCompanionFlow().map { entity ->
            entity?.toDomain() ?: Companion()
        }
    }

    override suspend fun saveCompanion(companion: Companion): Result<Unit> = runCatching {
        companionDao.insertCompanion(CompanionEntity.fromDomain(companion))
        cloudSyncService?.backupCompanion(companion)
    }

    override suspend fun addExperience(category: CompanionCategory, amount: Int): Result<Companion> = runCatching {
        val current = companionDao.getCompanionById()?.toDomain() ?: Companion()

        val updatedHappiness = current.happinessXp + if (category == CompanionCategory.HAPPINESS) amount else 0
        val updatedEnergy = current.energyXp + if (category == CompanionCategory.ENERGY) amount else 0
        val updatedWisdom = current.wisdomXp + if (category == CompanionCategory.WISDOM) amount else 0
        val updatedCreativity = current.creativityXp + if (category == CompanionCategory.CREATIVITY) amount else 0
        val newTotalXp = updatedHappiness + updatedEnergy + updatedWisdom + updatedCreativity

        val newStage = CompanionGrowthRules.getStageForXp(newTotalXp)
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

        val updatedCompanion = current.copy(
            experiencePoints = newTotalXp,
            happinessXp = updatedHappiness,
            energyXp = updatedEnergy,
            wisdomXp = updatedWisdom,
            creativityXp = updatedCreativity,
            stage = newStage,
            momentumScore = MomentumCalculator.calculateBoostedScore(current.momentumScore, baseBoost = 2.0),
            lastUpdatedEpochMs = now
        )

        companionDao.insertCompanion(CompanionEntity.fromDomain(updatedCompanion))
        cloudSyncService?.backupCompanion(updatedCompanion)
        updatedCompanion
    }

    override suspend fun updateMomentum(newScore: Double): Result<Unit> = runCatching {
        val current = companionDao.getCompanionById()?.toDomain() ?: Companion()
        val clampedScore = newScore.coerceIn(MomentumCalculator.MIN_MOMENTUM, MomentumCalculator.MAX_MOMENTUM)
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val updated = current.copy(momentumScore = clampedScore, lastUpdatedEpochMs = now)
        companionDao.insertCompanion(CompanionEntity.fromDomain(updated))
        cloudSyncService?.backupCompanion(updated)
    }
}
