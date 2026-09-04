package org.awaremate.android

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.awaremate.shared.domain.service.MissedCheckInReminderScheduler
import org.awaremate.shared.di.androidPlatformModule
import org.awaremate.shared.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AwareMateApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        org.awaremate.shared.util.AppStartupMetrics.recordAppStart()
        org.awaremate.shared.AppContextProvider.appContext = this
        val koinApplication = startKoin {
            androidLogger()
            androidContext(this@AwareMateApplication)
            modules(commonModule, androidPlatformModule)
        }
        applicationScope.launch {
            koinApplication.koin.get<MissedCheckInReminderScheduler>().refresh()
        }
    }
}

