package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionCategory

interface CompanionRepository {
    fun getCompanion(): Flow<Companion?>
    suspend fun saveCompanion(companion: Companion): Result<Unit>
    suspend fun addExperience(category: CompanionCategory, amount: Int): Result<Companion>
    suspend fun updateMomentum(newScore: Double): Result<Unit>
}
