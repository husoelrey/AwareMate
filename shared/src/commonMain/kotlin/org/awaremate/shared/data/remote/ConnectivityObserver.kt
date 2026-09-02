package org.awaremate.shared.data.remote

import kotlinx.coroutines.flow.StateFlow

/**
 * Multiplatform interface for observing network connectivity state.
 * Emits true when an active internet-capable network connection is available, false otherwise.
 */
interface ConnectivityObserver {
    val isOnline: StateFlow<Boolean>
}
