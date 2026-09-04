package org.awaremate.shared.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

private const val ACCOUNT_DELETION_TIMEOUT_MS = 20_000L
private const val FIRESTORE_SAFE_BATCH_SIZE = 450

class AndroidAccountDeletionService(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AccountDeletionService {

    override suspend fun deleteCloudDataAndAuthAccount(userId: String): Result<Unit> = runCatching {
        withTimeout(ACCOUNT_DELETION_TIMEOUT_MS) {
            val firebaseUser = firebaseAuth.currentUser
                ?: throw IllegalStateException("No signed-in account was found.")
            require(firebaseUser.uid == userId) { "The signed-in account does not match the local profile." }

            // All reads complete before the first destructive write, so a network failure cannot
            // leave a partially deleted Firestore dataset.
            val snapshots = loadUserDocuments(userId)
            deleteDocuments(snapshots)

            try {
                firebaseUser.delete().await()
            } catch (error: Throwable) {
                restoreDocuments(snapshots, error)
                if (error is FirebaseAuthRecentLoginRequiredException) {
                    throw RecentAuthenticationRequiredException(error)
                }
                throw error
            }
        }
    }

    private suspend fun deleteDocuments(snapshots: List<DocumentSnapshot>) {
        val deleted = mutableListOf<DocumentSnapshot>()
        try {
            snapshots.chunked(FIRESTORE_SAFE_BATCH_SIZE).forEach { chunk ->
                val deleteBatch = firestore.batch()
                chunk.forEach { deleteBatch.delete(it.reference) }
                deleteBatch.commit().await()
                deleted += chunk
            }
        } catch (error: Throwable) {
            restoreDocuments(deleted, error)
            throw error
        }
    }

    private suspend fun loadUserDocuments(userId: String): List<DocumentSnapshot> = coroutineScope {
        val directDocuments = listOf(
            async { firestore.collection("users").document(userId).get().await() },
            async { firestore.collection("companions").document(userId).get().await() }
        ).awaitAll().filter { it.exists() }

        val queriedDocuments = listOf(
            "mood_entries",
            "focus_sessions",
            "daily_challenges"
        ).map { collection ->
            async {
                firestore.collection(collection)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                    .documents
            }
        }.awaitAll().flatten()

        (directDocuments + queriedDocuments).distinctBy { it.reference.path }
    }

    private suspend fun restoreDocuments(
        snapshots: List<DocumentSnapshot>,
        authFailure: Throwable
    ) {
        if (snapshots.isEmpty()) return
        snapshots.chunked(FIRESTORE_SAFE_BATCH_SIZE).forEach { chunk ->
            runCatching {
                val restoreBatch = firestore.batch()
                chunk.forEach { snapshot ->
                    val data = snapshot.data ?: emptyMap()
                    restoreBatch.set(snapshot.reference, data)
                }
                restoreBatch.commit().await()
            }.exceptionOrNull()?.let(authFailure::addSuppressed)
        }
    }
}
