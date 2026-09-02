package org.awaremate.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.awaremate.shared.domain.model.SelfDiscoveryPrompt

@Entity(tableName = "self_discovery_prompts")
data class SelfDiscoveryPromptEntity(
    @PrimaryKey
    val id: String,
    val category: String,
    val question: String,
    val curiosityHint: String,
    val isAcknowledged: Boolean = false,
    val userReflection: String? = null,
    val lastAnsweredEpochMs: Long? = null
) {
    fun toDomain(): SelfDiscoveryPrompt = SelfDiscoveryPrompt(
        id = id,
        category = category,
        question = question,
        curiosityHint = curiosityHint,
        isAcknowledged = isAcknowledged,
        userReflection = userReflection,
        lastAnsweredEpochMs = lastAnsweredEpochMs
    )

    companion object {
        fun fromDomain(prompt: SelfDiscoveryPrompt): SelfDiscoveryPromptEntity = SelfDiscoveryPromptEntity(
            id = prompt.id,
            category = prompt.category,
            question = prompt.question,
            curiosityHint = prompt.curiosityHint,
            isAcknowledged = prompt.isAcknowledged,
            userReflection = prompt.userReflection,
            lastAnsweredEpochMs = prompt.lastAnsweredEpochMs
        )
    }
}
