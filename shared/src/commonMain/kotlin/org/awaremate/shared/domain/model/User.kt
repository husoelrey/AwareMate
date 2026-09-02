package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val createdAtEpochMs: Long = 0L
)
