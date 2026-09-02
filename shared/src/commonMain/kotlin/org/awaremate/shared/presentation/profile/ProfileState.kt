package org.awaremate.shared.presentation.profile

import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.User

data class ProfileState(
    val user: User? = null,
    val companion: Companion = Companion(),
    val daysActive: Int = 1,
    val totalXp: Int = 0,
    val completedChallengesCount: Int = 0,
    val isLoading: Boolean = false
)

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data class UpdateDisplayName(val newName: String) : ProfileIntent
}
