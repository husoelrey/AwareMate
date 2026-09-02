package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.MoodEntry

interface MoodRepository {
    fun getAllMoodEntries(): Flow<List<MoodEntry>>
    fun getMoodEntriesForRange(startEpochMs: Long, endEpochMs: Long): Flow<List<MoodEntry>>
    suspend fun insertMoodEntry(entry: MoodEntry): Result<Unit>
    suspend fun deleteMoodEntry(id: String): Result<Unit>
}
