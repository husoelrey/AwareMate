package org.awaremate.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SelfDiscoveryPrompt(
    val id: String,
    val category: String,
    val question: String,
    val curiosityHint: String,
    val isAcknowledged: Boolean = false,
    val userReflection: String? = null,
    val lastAnsweredEpochMs: Long? = null
)
