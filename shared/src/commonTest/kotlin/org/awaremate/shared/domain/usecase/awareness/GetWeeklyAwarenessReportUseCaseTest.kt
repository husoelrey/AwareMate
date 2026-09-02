package org.awaremate.shared.domain.usecase.awareness

import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.local.entity.CompanionEntity
import org.awaremate.shared.data.local.entity.FocusSessionEntity
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.data.repository.FocusSessionRepositoryImpl
import org.awaremate.shared.data.repository.UsageStatsRepositoryImpl
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.FocusCategory
import org.awaremate.shared.domain.model.UserPreferences
import org.awaremate.shared.test.FakeCompanionDao
import org.awaremate.shared.test.FakeFocusSessionDao
import org.awaremate.shared.test.FakePreferencesRepository
import org.awaremate.shared.test.FakeScreenTimeDao
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetWeeklyAwarenessReportUseCaseTest {

    private lateinit var fakeScreenTimeDao: FakeScreenTimeDao
    private lateinit var fakeFocusSessionDao: FakeFocusSessionDao
    private lateinit var fakeCompanionDao: FakeCompanionDao
    private lateinit var fakePreferencesRepo: FakePreferencesRepository
    private lateinit var useCase: GetWeeklyAwarenessReportUseCase

    @BeforeTest
    fun setUp() {
        fakeScreenTimeDao = FakeScreenTimeDao()
        fakeFocusSessionDao = FakeFocusSessionDao()
        fakeCompanionDao = FakeCompanionDao()
        fakePreferencesRepo = FakePreferencesRepository(
            initialPreferences = UserPreferences(dailyScreenTimeGoalMinutes = 180)
        )

        val usageRepo = UsageStatsRepositoryImpl(screenTimeDao = fakeScreenTimeDao)
        val focusRepo = FocusSessionRepositoryImpl(focusSessionDao = fakeFocusSessionDao)
        val companionRepo = CompanionRepositoryImpl(companionDao = fakeCompanionDao)

        useCase = GetWeeklyAwarenessReportUseCase(
            usageStatsRepository = usageRepo,
            focusSessionRepository = focusRepo,
            companionRepository = companionRepo,
            preferencesRepository = fakePreferencesRepo,
            calculateAwarenessScoreUseCase = CalculateAwarenessScoreUseCase()
        )
    }

    @Test
    fun testWeeklyReportAggregatesSevenDays() = runTest {
        val now = 1750000000000L

        fakeCompanionDao.insertCompanion(
            CompanionEntity.fromDomain(
                Companion(id = "primary", momentumScore = 85.0)
            )
        )

        fakeFocusSessionDao.insertSession(
            FocusSessionEntity(
                id = "f1",
                userId = "u1",
                startTimeEpochMs = now - (2 * 24 * 3600 * 1000L),
                durationSeconds = 50 * 60, // 50 min
                category = FocusCategory.DEEP_WORK.name,
                completed = true
            )
        )

        val report = useCase(referenceEpochMs = now)

        assertEquals(7, report.dailySummaries.size)
        assertTrue(report.totalScreenTimeMinutes > 0)
        assertTrue(report.dailyAverageMinutes > 0)
        assertEquals(50, report.totalFocusMinutes)
        assertEquals(1, report.focusSessionsCount)
        assertTrue(report.averageAwarenessScore in 0..100)
        assertTrue(report.momentumTrend.contains("Thriving"))
        assertNotNull(report.compassionateInsight)

        // Verify compassionate, non-punitive tone
        val insightLower = report.compassionateInsight.lowercase()
        assertFalse(insightLower.contains("fail"))
        assertFalse(insightLower.contains("too much"))
        assertFalse(insightLower.contains("wasted"))
        assertFalse(insightLower.contains("bad"))
    }
}
