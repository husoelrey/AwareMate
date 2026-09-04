package org.awaremate.shared.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.awaremate.shared.data.local.database.AwareMateDatabase
import org.awaremate.shared.data.local.database.getAndroidDatabaseBuilder
import org.awaremate.shared.data.local.datastore.createDataStore
import org.awaremate.shared.data.remote.AndroidAuthService
import org.awaremate.shared.data.remote.AndroidFirestoreSyncService
import org.awaremate.shared.data.remote.AuthService
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.data.repository.AndroidUsageStatsRepository
import org.awaremate.shared.data.service.AndroidNotificationService
import org.awaremate.shared.data.worker.AndroidMissedCheckInReminderScheduler
import org.awaremate.shared.domain.repository.UsageStatsRepository
import org.awaremate.shared.domain.service.NotificationService
import org.awaremate.shared.domain.service.MissedCheckInReminderScheduler
import org.koin.dsl.module

val androidPlatformModule = module {
    // Room KMP Android Database
    single<AwareMateDatabase> {
        getAndroidDatabaseBuilder(get<Context>()).build()
    }

    // Android DataStore instance
    single<DataStore<Preferences>> {
        val context = get<Context>()
        createDataStore(producePath = {
            context.filesDir.resolve("awaremate.preferences_pb").absolutePath
        })
    }

    // Firebase Auth & Firestore remote services
    single<AuthService> { AndroidAuthService() }
    single<CloudSyncService> { AndroidFirestoreSyncService() }

    // Android UsageStats & Notification Services
    single<UsageStatsRepository> {
        AndroidUsageStatsRepository(context = get(), screenTimeDao = get())
    }
    single<NotificationService> {
        AndroidNotificationService(context = get())
    }
    single<MissedCheckInReminderScheduler> {
        AndroidMissedCheckInReminderScheduler(context = get(), preferencesRepository = get())
    }

    // Network Connectivity Observer
    single<org.awaremate.shared.data.remote.ConnectivityObserver> {
        org.awaremate.shared.data.remote.AndroidConnectivityObserver(context = get())
    }
}
