package org.awaremate.shared.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.awaremate.shared.domain.model.FocusCategory
import org.awaremate.shared.domain.model.FocusSession

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val startTimeEpochMs: Long,
    val durationSeconds: Int,
    val category: String = FocusCategory.DEEP_WORK.name,
    val earnedXp: Int = 0,
    val completed: Boolean = true,
    val note: String? = null,
    val isSynced: Boolean = false
) {
    fun toDomain(): FocusSession = FocusSession(
        id = id,
        userId = userId,
        startTimeEpochMs = startTimeEpochMs,
        durationSeconds = durationSeconds,
        category = runCatching { FocusCategory.valueOf(category) }.getOrDefault(FocusCategory.DEEP_WORK),
        earnedXp = earnedXp,
        completed = completed,
        note = note,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(session: FocusSession): FocusSessionEntity = FocusSessionEntity(
            id = session.id,
            userId = session.userId,
            startTimeEpochMs = session.startTimeEpochMs,
            durationSeconds = session.durationSeconds,
            category = session.category.name,
            earnedXp = session.earnedXp,
            completed = session.completed,
            note = session.note,
            isSynced = session.isSynced
        )
    }
}
