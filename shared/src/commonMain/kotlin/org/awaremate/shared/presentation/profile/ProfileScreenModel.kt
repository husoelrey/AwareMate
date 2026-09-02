package org.awaremate.shared.presentation.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.User
import org.awaremate.shared.domain.repository.UserRepository
import org.awaremate.shared.domain.usecase.companion.GetCompanionUseCase

class ProfileScreenModel(
    private val userRepository: UserRepository,
    private val getCompanionUseCase: GetCompanionUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        screenModelScope.launch {
            launch {
                userRepository.getCurrentUser().collect { user ->
                    _state.update { it.copy(user = user, isLoading = false) }
                }
            }

            launch {
                getCompanionUseCase().collect { companion ->
                    val comp = companion ?: Companion()
                    _state.update {
                        it.copy(
                            companion = comp,
                            totalXp = comp.experiencePoints
                        )
                    }
                }
            }
        }
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile -> loadProfileData()

            is ProfileIntent.UpdateDisplayName -> {
                screenModelScope.launch {
                    val currentUser = _state.value.user ?: User(id = "local_user", displayName = "Mindful Friend")
                    val updated = currentUser.copy(displayName = intent.newName)
                    userRepository.saveUser(updated)
                }
            }
        }
    }
}
