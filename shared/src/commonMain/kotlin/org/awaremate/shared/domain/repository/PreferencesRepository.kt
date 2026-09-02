package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.UserPreferences

interface PreferencesRepository {
    fun getPreferences(): Flow<UserPreferences>
    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences): Result<Unit>
    suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit>
}
