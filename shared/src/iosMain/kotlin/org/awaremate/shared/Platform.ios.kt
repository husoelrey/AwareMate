package org.awaremate.shared

class IOSPlatform : Platform {
    override val name: String = "iOS"
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun hasUsageStatsPermission(context: Any?): Boolean = true

actual fun openUsageAccessSettings(context: Any?) {
    // No-op for iOS target in v1.0
}
