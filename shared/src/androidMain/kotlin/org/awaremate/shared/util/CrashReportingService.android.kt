package org.awaremate.shared.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

class AndroidCrashReportingService : CrashReportingService {

    override fun log(message: String) {
        try {
            FirebaseCrashlytics.getInstance().log(message)
        } catch (_: Throwable) {
            // Gracefully handle uninitialized Firebase or testing
        }
    }

    override fun recordException(throwable: Throwable) {
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (_: Throwable) {
            // Gracefully handle uninitialized Firebase or testing
        }
    }
}

actual fun createCrashReportingService(): CrashReportingService = AndroidCrashReportingService()
