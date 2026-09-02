package org.awaremate.shared.domain.usecase.companion

import kotlinx.coroutines.flow.firstOrNull
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionEmotionStateMachine
import org.awaremate.shared.domain.model.CompanionEvent
import org.awaremate.shared.domain.repository.CompanionRepository

class UpdateCompanionEmotionUseCase(
    private val companionRepository: CompanionRepository
) {
    suspend operator fun invoke(event: CompanionEvent): Result<Companion> = runCatching {
        val current = companionRepository.getCompanion().firstOrNull() ?: Companion()
        val nextEmotion = CompanionEmotionStateMachine.transition(current.emotion, event)

        if (nextEmotion == current.emotion) {
            return@runCatching current
        }

        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val updated = current.copy(
            emotion = nextEmotion,
            lastUpdatedEpochMs = now
        )

        companionRepository.saveCompanion(updated).getOrThrow()
        updated
    }
}
