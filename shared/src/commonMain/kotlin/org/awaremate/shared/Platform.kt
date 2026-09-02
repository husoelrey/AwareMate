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
