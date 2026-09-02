package org.awaremate.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.data.local.entity.MoodEntryEntity

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestampEpochMs DESC")
    fun getAllMoodEntriesFlow(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE timestampEpochMs BETWEEN :startEpochMs AND :endEpochMs ORDER BY timestampEpochMs ASC")
    fun getMoodEntriesForRange(startEpochMs: Long, endEpochMs: Long): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<MoodEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntryEntity)

    @Query("UPDATE mood_entries SET isSynced = :synced WHERE id = :id")
    suspend fun markAsSynced(id: String, synced: Boolean = true)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMoodEntry(id: String)

    @Query("DELETE FROM mood_entries")
    suspend fun clearAllMoodEntries()
}
