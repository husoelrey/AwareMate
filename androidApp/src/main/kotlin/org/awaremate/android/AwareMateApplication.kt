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
        startKoin {
            androidLogger()
            androidContext(this@AwareMateApplication)
            modules(commonModule, androidPlatformModule)
        }
    }
}

