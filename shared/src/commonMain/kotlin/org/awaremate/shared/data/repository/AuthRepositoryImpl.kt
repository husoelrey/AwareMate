package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.data.local.dao.UserDao
import org.awaremate.shared.data.local.entity.UserEntity
import org.awaremate.shared.data.remote.AuthService
import org.awaremate.shared.domain.model.User
import org.awaremate.shared.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userDao: UserDao
) : AuthRepository {

    override fun observeAuthState(): Flow<User?> = authService.authState

    override fun getCurrentUser(): User? = authService.currentUser

    override suspend fun signInAnonymously(): Result<User> = runCatching {
        val user = authService.signInAnonymously().getOrThrow()
        userDao.insertUser(UserEntity.fromDomain(user))
        user
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val user = authService.signInWithGoogle(idToken).getOrThrow()
        userDao.insertUser(UserEntity.fromDomain(user))
        user
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        authService.signOut().getOrThrow()
        userDao.clearAllUsers()
    }
}
