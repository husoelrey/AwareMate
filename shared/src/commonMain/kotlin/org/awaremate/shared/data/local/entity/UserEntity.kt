package org.awaremate.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.awaremate.shared.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val displayName: String,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val createdAtEpochMs: Long = 0L,
    val lastActiveEpochMs: Long = 0L
) {
    fun toDomain(): User = User(
        id = id,
        displayName = displayName,
        email = email,
        isAnonymous = isAnonymous,
        createdAtEpochMs = createdAtEpochMs,
        lastActiveEpochMs = lastActiveEpochMs
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            displayName = user.displayName,
            email = user.email,
            isAnonymous = user.isAnonymous,
            createdAtEpochMs = user.createdAtEpochMs,
            lastActiveEpochMs = user.lastActiveEpochMs
        )
    }
}
