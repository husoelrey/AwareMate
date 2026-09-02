package org.awaremate.shared.data.remote

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.User

interface AuthService {
    val authState: Flow<User?>
    val currentUser: User?
    suspend fun signInAnonymously(): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
}
