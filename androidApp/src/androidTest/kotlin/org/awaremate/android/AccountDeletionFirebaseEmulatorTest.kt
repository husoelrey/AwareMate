package org.awaremate.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.runBlocking
import org.awaremate.shared.data.remote.AndroidAccountDeletionService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountDeletionFirebaseEmulatorTest {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @Before
    fun connectToEmulators() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth.useEmulator("10.0.2.2", 9099)
        firestore.useEmulator("10.0.2.2", 8080)
    }

    @Test
    fun deletesFirestoreDocumentsAndFirebaseAuthAccount() = runBlocking {
        auth.signOut()
        val user = Tasks.await(auth.signInAnonymously()).user
            ?: error("Auth emulator did not create an anonymous user")
        val userId = user.uid

        Tasks.await(firestore.collection("users").document(userId).set(mapOf("name" to "audit")))
        Tasks.await(firestore.collection("companions").document(userId).set(mapOf("stage" to "sprout")))
        Tasks.await(
            firestore.collection("mood_entries").document("audit-mood")
                .set(mapOf("userId" to userId, "moodScore" to 4))
        )

        val result = AndroidAccountDeletionService(auth, firestore)
            .deleteCloudDataAndAuthAccount(userId)

        assertTrue(result.exceptionOrNull()?.stackTraceToString(), result.isSuccess)
        assertFalse(
            Tasks.await(firestore.collection("users").document(userId).get(Source.SERVER)).exists()
        )
        assertFalse(
            Tasks.await(firestore.collection("companions").document(userId).get(Source.SERVER)).exists()
        )
        assertTrue(
            Tasks.await(
                firestore.collection("mood_entries").whereEqualTo("userId", userId).get(Source.SERVER)
            ).isEmpty
        )
        assertNull(auth.currentUser)
    }
}
