package org.awaremate.shared.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.awaremate.shared.AppContextProvider

class AndroidAnalyticsService : AnalyticsService {

    override fun logEvent(eventName: String, params: Map<String, String>) {
        try {
            val context = AppContextProvider.appContext ?: return
            val bundle = Bundle().apply {
                params.forEach { (key, value) ->
                    putString(key, value)
                }
            }
            FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle)
        } catch (_: Throwable) {
            // Gracefully handle uninitialized Firebase or test environment
        }
    }
}

actual fun createAnalyticsService(): AnalyticsService = AndroidAnalyticsService()
