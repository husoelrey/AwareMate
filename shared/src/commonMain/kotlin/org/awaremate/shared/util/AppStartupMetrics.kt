package org.awaremate.shared.util

import kotlinx.datetime.Clock

/**
 * Utility for measuring and observing AwareMate cold startup metrics.
 */
object AppStartupMetrics {

    private var appStartTimeEpochMs: Long = 0L
    private var firstFrameTimeEpochMs: Long = 0L

    fun recordAppStart() {
        if (appStartTimeEpochMs == 0L) {
            appStartTimeEpochMs = Clock.System.now().toEpochMilliseconds()
        }
    }

    fun recordFirstFrameRender() {
        if (firstFrameTimeEpochMs == 0L && appStartTimeEpochMs != 0L) {
            firstFrameTimeEpochMs = Clock.System.now().toEpochMilliseconds()
        }
    }

    val startupDurationMs: Long
        get() = if (appStartTimeEpochMs != 0L && firstFrameTimeEpochMs != 0L) {
            (firstFrameTimeEpochMs - appStartTimeEpochMs).coerceAtLeast(0L)
        } else {
            0L
        }

    val isColdStartOptimal: Boolean
        get() = startupDurationMs in 1..1500L
}
