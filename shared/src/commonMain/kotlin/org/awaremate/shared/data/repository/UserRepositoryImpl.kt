package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.awaremate.shared.data.local.dao.UserDao
import org.awaremate.shared.data.local.entity.UserEntity
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.domain.model.User
import org.awaremate.shared.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val cloudSyncService: CloudSyncService? = null
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUserFlow().map { it?.toDomain() }
    }

    override suspend fun saveUser(user: User): Result<Unit> = runCatching {
        userDao.insertUser(UserEntity.fromDomain(user))
        runCatching {
            kotlinx.coroutines.withTimeoutOrNull(1000L) {
                cloudSyncService?.backupUser(user)
            }
        }
        Unit
    }

    override suspend fun getUser(id: String): User? {
        return userDao.getUserById(id)?.toDomain()
    }

    override suspend fun deleteUser(id: String): Result<Unit> = runCatching {
        userDao.deleteUser(id)
    }
}
