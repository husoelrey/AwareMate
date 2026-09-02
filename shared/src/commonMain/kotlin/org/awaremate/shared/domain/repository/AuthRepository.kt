package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.User

interface AuthRepository {
    fun observeAuthState(): Flow<User?>
    fun getCurrentUser(): User?
    suspend fun signInAnonymously(): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
}
