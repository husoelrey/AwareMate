package org.awaremate.shared.domain.repository

interface SyncRepository {
    suspend fun syncAll(): Result<Unit>
    suspend fun syncPendingItems(): Result<Unit>
}
