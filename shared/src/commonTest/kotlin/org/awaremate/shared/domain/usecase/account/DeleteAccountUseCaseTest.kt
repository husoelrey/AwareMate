package org.awaremate.shared.domain.usecase.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.remote.ConnectivityObserver
import org.awaremate.shared.domain.model.UserPreferences
import org.awaremate.shared.test.FakeAccountDataDao
import org.awaremate.shared.test.FakeAccountDeletionService
import org.awaremate.shared.test.FakeAuthService
import org.awaremate.shared.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class DeleteAccountUseCaseTest {

    @Test
    fun onlineDeletionClearsRemoteLocalPreferencesAndSession() = runTest {
        val connectivity = FakeConnectivityObserver(isOnline = true)
        val auth = FakeAuthService().also { it.signInAnonymously().getOrThrow() }
        val remote = FakeAccountDeletionService()
        val local = FakeAccountDataDao()
        val preferences = FakePreferencesRepository(
            UserPreferences(onboardingCompleted = true, themeMode = "DARK")
        )
        val useCase = DeleteAccountUseCase(connectivity, auth, remote, local, preferences)

        val result = useCase()

        assertEquals(DeleteAccountResult.Success, result)
        assertEquals("anon-123", remote.deletedUserId)
        assertEquals(1, local.clearCalls)
        assertFalse(preferences.getPreferences().first().onboardingCompleted)
        assertEquals("SYSTEM", preferences.getPreferences().first().themeMode)
        assertEquals(1, auth.signOutCalls)
        assertNull(auth.currentUser)
    }

    @Test
    fun offlineDeletionMakesNoChanges() = runTest {
        val connectivity = FakeConnectivityObserver(isOnline = false)
        val auth = FakeAuthService().also { it.signInAnonymously().getOrThrow() }
        val remote = FakeAccountDeletionService()
        val local = FakeAccountDataDao()
        val preferences = FakePreferencesRepository(UserPreferences(onboardingCompleted = true))
        val useCase = DeleteAccountUseCase(connectivity, auth, remote, local, preferences)

        val result = useCase()

        assertEquals(DeleteAccountResult.Offline, result)
        assertNull(remote.deletedUserId)
        assertEquals(0, local.clearCalls)
        assertEquals(true, preferences.getPreferences().first().onboardingCompleted)
        assertEquals(0, auth.signOutCalls)
        assertEquals("anon-123", auth.currentUser?.id)
    }

    @Test
    fun localOnlyUserCanDeleteWithoutNetwork() = runTest {
        val connectivity = FakeConnectivityObserver(isOnline = false)
        val auth = FakeAuthService()
        val remote = FakeAccountDeletionService()
        val local = FakeAccountDataDao()
        val preferences = FakePreferencesRepository(UserPreferences(onboardingCompleted = true))
        val useCase = DeleteAccountUseCase(connectivity, auth, remote, local, preferences)

        val result = useCase()

        assertEquals(DeleteAccountResult.Success, result)
        assertNull(remote.deletedUserId)
        assertEquals(1, local.clearCalls)
        assertFalse(preferences.getPreferences().first().onboardingCompleted)
    }
}

private class FakeConnectivityObserver(isOnline: Boolean) : ConnectivityObserver {
    override val isOnline = MutableStateFlow(isOnline)
}
