package org.awaremate.shared

/**
 * Platform interface representing device platform details.
 */
interface Platform {
    val name: String
}

/**
 * Returns the current runtime platform information.
 */
expect fun getPlatform(): Platform

/**
 * Checks whether the app has been granted usage stats access.
 * On Android, checks AppOpsManager for PACKAGE_USAGE_STATS permission.
 */
expect fun hasUsageStatsPermission(context: Any? = null): Boolean

/**
 * Opens the system settings screen for Usage Access permissions.
 * On Android, launches Settings.ACTION_USAGE_ACCESS_SETTINGS.
 */
expect fun openUsageAccessSettings(context: Any? = null)
