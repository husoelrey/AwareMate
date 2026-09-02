package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.User

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun getUser(id: String): User?
    suspend fun deleteUser(id: String): Result<Unit>
}
