package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.FocusSession

interface FocusSessionRepository {
    fun getRecentSessions(limit: Int = 20): Flow<List<FocusSession>>
    suspend fun saveSession(session: FocusSession): Result<Unit>
    suspend fun getTotalFocusMinutes(): Long
}
