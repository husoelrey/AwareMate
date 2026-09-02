package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.awaremate.shared.data.local.dao.SelfDiscoveryPromptDao
import org.awaremate.shared.data.local.entity.SelfDiscoveryPromptEntity
import org.awaremate.shared.domain.model.SelfDiscoveryCatalog
import org.awaremate.shared.domain.model.SelfDiscoveryPrompt
import org.awaremate.shared.domain.repository.SelfDiscoveryRepository

class SelfDiscoveryRepositoryImpl(
    private val promptDao: SelfDiscoveryPromptDao
) : SelfDiscoveryRepository {

    override fun getAllPrompts(): Flow<List<SelfDiscoveryPrompt>> {
        return promptDao.getAllPromptsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPromptById(id: String): SelfDiscoveryPrompt? {
        return promptDao.getPromptById(id)?.toDomain()
    }

    override suspend fun initializeDefaultPrompts(): Result<Unit> = runCatching {
        val count = promptDao.getPromptCount()
        if (count == 0) {
            val defaultEntities = SelfDiscoveryCatalog.starterPrompts.map {
                SelfDiscoveryPromptEntity.fromDomain(it)
            }
            promptDao.insertDefaultPrompts(defaultEntities)
        }
    }

    override suspend fun recordObservation(promptId: String, reflection: String?): Result<Unit> = runCatching {
        val now = Clock.System.now().toEpochMilliseconds()
        promptDao.savePromptReflection(promptId, reflection, now)
    }
}
