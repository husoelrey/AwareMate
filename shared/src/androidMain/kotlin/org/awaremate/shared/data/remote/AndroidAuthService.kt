package org.awaremate.shared.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.awaremate.shared.domain.model.User

class AndroidAuthService(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthService {

    override val authState: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.toDomain()
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override val currentUser: User?
        get() = runCatching { firebaseAuth.currentUser?.toDomain() }.getOrNull()

    override suspend fun signInAnonymously(): Result<User> = runCatching {
        val authResult = firebaseAuth.signInAnonymously().await()
        val user = authResult.user?.toDomain()
            ?: throw IllegalStateException("Firebase anonymous sign-in returned null user")
        user
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val user = authResult.user?.toDomain()
            ?: throw IllegalStateException("Firebase Google sign-in returned null user")
        user
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        firebaseAuth.signOut()
    }

    private fun FirebaseUser.toDomain(): User {
        return User(
            id = uid,
            displayName = displayName ?: if (isAnonymous) "Explorer" else "AwareMate User",
            email = email,
            isAnonymous = isAnonymous,
            createdAtEpochMs = metadata?.creationTimestamp ?: 0L,
            lastActiveEpochMs = metadata?.lastSignInTimestamp ?: 0L
        )
    }
}
