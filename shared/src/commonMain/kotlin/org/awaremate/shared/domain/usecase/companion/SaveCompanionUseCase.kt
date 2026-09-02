package org.awaremate.shared.domain.usecase.companion

import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.repository.CompanionRepository

class SaveCompanionUseCase(
    private val companionRepository: CompanionRepository
) {
    suspend operator fun invoke(companion: Companion): Result<Unit> {
        return companionRepository.saveCompanion(companion)
    }
}
