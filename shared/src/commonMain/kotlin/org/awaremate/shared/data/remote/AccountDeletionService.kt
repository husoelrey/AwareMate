package org.awaremate.shared.data.remote

/**
 * Performs the coupled, remote portion of account deletion.
 *
 * Implementations must remove the user's cloud records and Firebase Auth account as one
 * recoverable operation: if Auth deletion fails, already-removed cloud records are restored.
 */
interface AccountDeletionService {
    suspend fun deleteCloudDataAndAuthAccount(userId: String): Result<Unit>
}

class RecentAuthenticationRequiredException(
    cause: Throwable? = null
) : Exception("Please sign in again before deleting your account.", cause)
