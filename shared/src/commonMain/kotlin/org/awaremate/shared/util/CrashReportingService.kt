package org.awaremate.shared.util

/**
 * Multiplatform abstraction for logging errors and non-fatal exceptions to Crashlytics.
 */
interface CrashReportingService {
    fun log(message: String)
    fun recordException(throwable: Throwable)
}

expect fun createCrashReportingService(): CrashReportingService
