package org.awaremate.shared.util

class IosCrashReportingService : CrashReportingService {
    override fun log(message: String) {}
    override fun recordException(throwable: Throwable) {}
}

actual fun createCrashReportingService(): CrashReportingService = IosCrashReportingService()
