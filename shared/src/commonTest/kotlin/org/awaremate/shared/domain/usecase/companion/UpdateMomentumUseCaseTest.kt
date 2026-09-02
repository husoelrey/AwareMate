package org.awaremate.shared.domain.usecase.companion

import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.test.FakeCompanionDao
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateMomentumUseCaseTest {

    @Test
    fun testApplyInactivityDecayNonPunitive() = runTest {
        val dao = FakeCompanionDao()
        val repo = CompanionRepositoryImpl(dao)
        val useCase = UpdateMomentumUseCase(repo)

        // Seed with 100.0 momentum
        repo.saveCompanion(Companion(momentumScore = 100.0))

        // 1 day missed -> 90.0 (Compassionate decay)
        val day1 = useCase.applyInactivityDecay(daysInactive = 1).getOrThrow()
        assertEquals(90.0, day1.momentumScore, 0.01)
        assertEquals(CompanionEmotion.PEACEFUL, day1.emotion)

        // Reset to 100.0 to test 3 days missed in isolation -> 72.9 and transitions emotion to RESTING
        repo.saveCompanion(Companion(momentumScore = 100.0, emotion = CompanionEmotion.PEACEFUL))
        val day3 = useCase.applyInactivityDecay(daysInactive = 3).getOrThrow()
        assertEquals(72.9, day3.momentumScore, 0.01)
        assertEquals(CompanionEmotion.RESTING, day3.emotion)
        assertTrue(day3.momentumScore > 50.0, "Score stays respectable after a 3-day break")
    }

    @Test
    fun testBoostMomentumWithComebackBonus() = runTest {
        val dao = FakeCompanionDao()
        val repo = CompanionRepositoryImpl(dao)
        val useCase = UpdateMomentumUseCase(repo)

        repo.saveCompanion(Companion(momentumScore = 50.0))

        // Comeback bonus after 2+ days absence: 50 + (10 * 1.5) = 65.0
        val boosted = useCase.boostMomentum(baseBoost = 10.0, daysInactive = 2).getOrThrow()
        assertEquals(65.0, boosted.momentumScore, 0.01)
    }
}
