package org.awaremate.shared.util

/**
 * Multiplatform abstraction for logging non-invasive, privacy-respecting analytics events.
 */
interface AnalyticsService {
    fun logEvent(eventName: String, params: Map<String, String> = emptyMap())
}

expect fun createAnalyticsService(): AnalyticsService
