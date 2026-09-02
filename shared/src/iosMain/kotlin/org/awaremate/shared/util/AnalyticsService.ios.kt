package org.awaremate.shared.util

class IosAnalyticsService : AnalyticsService {
    override fun logEvent(eventName: String, params: Map<String, String>) {}
}

actual fun createAnalyticsService(): AnalyticsService = IosAnalyticsService()
