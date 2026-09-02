package org.awaremate.android

import android.app.Application
import org.awaremate.shared.di.androidPlatformModule
import org.awaremate.shared.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AwareMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        org.awaremate.shared.util.AppStartupMetrics.recordAppStart()
        org.awaremate.shared.AppContextProvider.appContext = this
        startKoin {
            androidLogger()
            androidContext(this@AwareMateApplication)
            modules(commonModule, androidPlatformModule)
        }
    }
}

