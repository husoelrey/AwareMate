package org.awaremate.shared.di

import org.awaremate.shared.data.local.database.AwareMateDatabase
import org.awaremate.shared.data.local.datastore.PreferencesRepositoryImpl
import org.awaremate.shared.data.repository.AuthRepositoryImpl
import org.awaremate.shared.data.repository.CompanionRepositoryImpl
import org.awaremate.shared.data.repository.DailyChallengeRepositoryImpl
import org.awaremate.shared.data.repository.FocusSessionRepositoryImpl
import org.awaremate.shared.data.repository.MoodRepositoryImpl
import org.awaremate.shared.data.repository.SyncRepositoryImpl
import org.awaremate.shared.data.repository.UserRepositoryImpl
import org.awaremate.shared.domain.repository.AuthRepository
import org.awaremate.shared.domain.repository.CompanionRepository
import org.awaremate.shared.domain.repository.DailyChallengeRepository
import org.awaremate.shared.domain.repository.FocusSessionRepository
import org.awaremate.shared.domain.repository.MoodRepository
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.domain.repository.SyncRepository
import org.awaremate.shared.domain.repository.UserRepository
import org.awaremate.shared.presentation.companion.CompanionScreenModel
import org.awaremate.shared.presentation.home.HomeScreenModel
import org.awaremate.shared.presentation.onboarding.OnboardingScreenModel
import org.awaremate.shared.presentation.profile.ProfileScreenModel
import org.awaremate.shared.presentation.settings.SettingsScreenModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    // DAOs extracted from Room Database
    single { get<AwareMateDatabase>().userDao() }
    single { get<AwareMateDatabase>().companionDao() }
    single { get<AwareMateDatabase>().moodEntryDao() }
    single { get<AwareMateDatabase>().focusSessionDao() }
    single { get<AwareMateDatabase>().dailyChallengeDao() }
    single { get<AwareMateDatabase>().screenTimeDao() }

    // DataStore Preferences Repository
    single<PreferencesRepository> { PreferencesRepositoryImpl(dataStore = get()) }

    // Domain Repositories (Local-first Room SSOT)
    single<UserRepository> { UserRepositoryImpl(userDao = get(), cloudSyncService = getOrNull()) }
    single<CompanionRepository> { CompanionRepositoryImpl(companionDao = get(), cloudSyncService = getOrNull()) }
    single<MoodRepository> { MoodRepositoryImpl(moodEntryDao = get(), cloudSyncService = getOrNull()) }
    single<FocusSessionRepository> { FocusSessionRepositoryImpl(focusSessionDao = get(), cloudSyncService = getOrNull()) }
    single<DailyChallengeRepository> { DailyChallengeRepositoryImpl(dailyChallengeDao = get(), cloudSyncService = getOrNull()) }
    single<org.awaremate.shared.domain.repository.UsageStatsRepository> {
        org.awaremate.shared.data.repository.UsageStatsRepositoryImpl(screenTimeDao = get())
    }

    // Auth & Sync Repositories
    single<AuthRepository> { AuthRepositoryImpl(authService = get(), userDao = get()) }
    single<SyncRepository> {
        SyncRepositoryImpl(
            userDao = get(),
            companionDao = get(),
            moodEntryDao = get(),
            focusSessionDao = get(),
            dailyChallengeDao = get(),
            cloudSyncService = get()
        )
    }

    // P3 Domain Use Cases & Engines
    single { org.awaremate.shared.domain.usecase.companion.CalculateGrowthStageUseCase() }
    single { org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase(companionRepository = get()) }
    single { org.awaremate.shared.domain.usecase.companion.UpdateMomentumUseCase(companionRepository = get()) }
    single { org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase(companionRepository = get()) }
    single { org.awaremate.shared.domain.usecase.companion.GetCompanionUseCase(companionRepository = get()) }
    single { org.awaremate.shared.domain.usecase.companion.SaveCompanionUseCase(companionRepository = get()) }
    single { org.awaremate.shared.domain.usecase.challenge.GenerateDailyChallengesUseCase(dailyChallengeRepository = get()) }
    single { org.awaremate.shared.domain.usecase.challenge.GetDailyChallengesUseCase(dailyChallengeRepository = get()) }
    single { org.awaremate.shared.domain.usecase.challenge.CompleteDailyChallengeUseCase(dailyChallengeRepository = get(), addExperienceUseCase = get()) }
    single {
        org.awaremate.shared.domain.usecase.awareness.CalculateAwarenessScoreUseCase(
            moodRepository = getOrNull(),
            focusSessionRepository = getOrNull(),
            dailyChallengeRepository = getOrNull(),
            preferencesRepository = getOrNull()
        )
    }

    // P5 Digital Awareness Use Cases
    single { org.awaremate.shared.domain.usecase.sunset.DigitalSunsetUseCase(preferencesRepository = get()) }
    single {
        org.awaremate.shared.domain.usecase.awareness.GetWeeklyAwarenessReportUseCase(
            usageStatsRepository = get(),
            focusSessionRepository = get(),
            companionRepository = get(),
            preferencesRepository = get(),
            calculateAwarenessScoreUseCase = get()
        )
    }

    // Presentation ScreenModels
    factory { OnboardingScreenModel(preferencesRepository = get(), saveCompanionUseCase = get()) }
    factory {
        HomeScreenModel(
            getCompanionUseCase = get(),
            calculateGrowthStageUseCase = get(),
            calculateAwarenessScoreUseCase = get(),
            getDailyChallengesUseCase = get(),
            generateDailyChallengesUseCase = get(),
            completeDailyChallengeUseCase = get(),
            addExperienceUseCase = get(),
            updateMomentumUseCase = get(),
            updateCompanionEmotionUseCase = get()
        )
    }
    factory {
        CompanionScreenModel(
            getCompanionUseCase = get(),
            calculateGrowthStageUseCase = get(),
            addExperienceUseCase = get(),
            updateCompanionEmotionUseCase = get(),
            saveCompanionUseCase = get()
        )
    }
    factory {
        org.awaremate.shared.presentation.focus.FocusScreenModel(
            focusSessionRepository = get(),
            companionRepository = get(),
            addExperienceUseCase = get(),
            updateMomentumUseCase = get(),
            calculateGrowthStageUseCase = get()
        )
    }
    factory { SettingsScreenModel(preferencesRepository = get()) }
    factory {
        ProfileScreenModel(
            userRepository = get(),
            getCompanionUseCase = get()
        )
    }
    factory {
        org.awaremate.shared.presentation.analytics.ScreenTimeAnalyticsScreenModel(
            usageStatsRepository = get(),
            preferencesRepository = get()
        )
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }
