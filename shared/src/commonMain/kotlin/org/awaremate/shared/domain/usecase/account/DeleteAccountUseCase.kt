package org.awaremate.shared.domain.usecase.account

import org.awaremate.shared.data.local.dao.AccountDataDao
import org.awaremate.shared.data.remote.AccountDeletionService
import org.awaremate.shared.data.remote.AuthService
import org.awaremate.shared.data.remote.ConnectivityObserver
import org.awaremate.shared.data.remote.RecentAuthenticationRequiredException
import org.awaremate.shared.domain.model.UserPreferences
import org.awaremate.shared.domain.repository.PreferencesRepository

sealed interface DeleteAccountResult {
    data object Success : DeleteAccountResult
    data object Offline : DeleteAccountResult
    data object RecentAuthenticationRequired : DeleteAccountResult
    data class Failed(val message: String) : DeleteAccountResult
}

class DeleteAccountUseCase(
    private val connectivityObserver: ConnectivityObserver,
    private val authService: AuthService,
    private val accountDeletionService: AccountDeletionService,
    private val accountDataDao: AccountDataDao,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): DeleteAccountResult {
        val user = authService.currentUser
        if (user != null) {
            if (!connectivityObserver.isOnline.value) return DeleteAccountResult.Offline

            val remoteResult = accountDeletionService.deleteCloudDataAndAuthAccount(user.id)
            if (remoteResult.isFailure) {
                val error = remoteResult.exceptionOrNull()
                return if (error is RecentAuthenticationRequiredException) {
                    DeleteAccountResult.RecentAuthenticationRequired
                } else {
                    DeleteAccountResult.Failed(
                        error?.message ?: "We couldn't delete your account right now. Your local data is still here."
                    )
                }
            }
        }

        return runCatching {
            accountDataDao.clearAllAccountData()
            preferencesRepository.updatePreferences { UserPreferences() }.getOrThrow()
            authService.signOut().getOrThrow()
        }.fold(
            onSuccess = { DeleteAccountResult.Success },
            onFailure = {
                DeleteAccountResult.Failed(
                    "Your account was removed, but this device could not finish clearing its local copy. Please retry."
                )
            }
        )
    }
}
