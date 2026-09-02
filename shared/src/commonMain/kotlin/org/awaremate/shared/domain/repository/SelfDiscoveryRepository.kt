package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.SelfDiscoveryPrompt

interface SelfDiscoveryRepository {
    fun getAllPrompts(): Flow<List<SelfDiscoveryPrompt>>
    suspend fun getPromptById(id: String): SelfDiscoveryPrompt?
    suspend fun initializeDefaultPrompts(): Result<Unit>
    suspend fun recordObservation(promptId: String, reflection: String?): Result<Unit>
}
