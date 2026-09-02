package org.awaremate.shared.domain.usecase.companion

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.repository.CompanionRepository

class GetCompanionUseCase(
    private val companionRepository: CompanionRepository
) {
    operator fun invoke(): Flow<Companion?> {
        return companionRepository.getCompanion()
    }
}
