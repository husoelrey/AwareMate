package org.awaremate.shared.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.awaremate.shared.data.local.dao.CompanionDao
import org.awaremate.shared.data.local.dao.DailyChallengeDao
import org.awaremate.shared.data.local.dao.FocusSessionDao
import org.awaremate.shared.data.local.dao.MoodEntryDao
import org.awaremate.shared.data.local.dao.UserDao
import org.awaremate.shared.data.local.database.AwareMateDatabase
import org.awaremate.shared.data.local.datastore.createDataStore
import org.awaremate.shared.data.remote.AuthService
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.domain.repository.AuthRepository
import org.awaremate.shared.domain.repository.CompanionRepository
import org.awaremate.shared.domain.repository.DailyChallengeRepository
import org.awaremate.shared.domain.repository.FocusSessionRepository
import org.awaremate.shared.domain.repository.MoodRepository
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.domain.repository.SyncRepository
import org.awaremate.shared.domain.repository.UserRepository
import org.awaremate.shared.test.FakeAuthService
import org.awaremate.shared.test.FakeCloudSyncService
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class KoinDependencyGraphTest : KoinTest {

    private lateinit var database: AwareMateDatabase

    private val testPlatformModule = module {
        single<AwareMateDatabase> { database }
        single<DataStore<Preferences>> {
            val context = ApplicationProvider.getApplicationContext<Context>()
            createDataStore(producePath = { context.filesDir.resolve("test_prefs.preferences_pb").absolutePath })
        }
        single<AuthService> { FakeAuthService() }
        single<CloudSyncService> { FakeCloudSyncService() }
    }

    @BeforeTest
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AwareMateDatabase::class.java
        ).allowMainThreadQueries().build()

        startKoin {
            modules(commonModule, testPlatformModule)
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        database.close()
    }

    @Test
    fun testAllKoinDependenciesResolveWithoutCycle() {
        // Verify DAOs resolve
        assertNotNull(get<UserDao>())
        assertNotNull(get<CompanionDao>())
        assertNotNull(get<MoodEntryDao>())
        assertNotNull(get<FocusSessionDao>())
        assertNotNull(get<DailyChallengeDao>())
        assertNotNull(get<org.awaremate.shared.data.local.dao.ScreenTimeDao>())

        // Verify Repositories resolve
        assertNotNull(get<UserRepository>())
        assertNotNull(get<CompanionRepository>())
        assertNotNull(get<MoodRepository>())
        assertNotNull(get<FocusSessionRepository>())
        assertNotNull(get<DailyChallengeRepository>())
        assertNotNull(get<PreferencesRepository>())
        assertNotNull(get<AuthRepository>())
        assertNotNull(get<SyncRepository>())
        assertNotNull(get<org.awaremate.shared.domain.repository.UsageStatsRepository>())

        // Verify P3 & P5 Use Cases resolve
        assertNotNull(get<org.awaremate.shared.domain.usecase.companion.CalculateGrowthStageUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.companion.GetCompanionUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.companion.SaveCompanionUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.challenge.GenerateDailyChallengesUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.challenge.GetDailyChallengesUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.challenge.CompleteDailyChallengeUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.awareness.CalculateAwarenessScoreUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.sunset.DigitalSunsetUseCase>())
        assertNotNull(get<org.awaremate.shared.domain.usecase.awareness.GetWeeklyAwarenessReportUseCase>())

        // Verify ScreenModels resolve
        assertNotNull(get<org.awaremate.shared.presentation.onboarding.OnboardingScreenModel>())
        assertNotNull(get<org.awaremate.shared.presentation.home.HomeScreenModel>())
        assertNotNull(get<org.awaremate.shared.presentation.companion.CompanionScreenModel>())
        assertNotNull(get<org.awaremate.shared.presentation.focus.FocusScreenModel>())
        assertNotNull(get<org.awaremate.shared.presentation.settings.SettingsScreenModel>())
        assertNotNull(get<org.awaremate.shared.presentation.profile.ProfileScreenModel>())
    }
}
