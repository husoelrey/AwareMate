package org.awaremate.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.data.local.entity.FocusSessionEntity

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTimeEpochMs DESC LIMIT :limit")
    fun getRecentSessionsFlow(limit: Int): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<FocusSessionEntity>

    @Query("SELECT SUM(durationSeconds) FROM focus_sessions WHERE completed = 1")
    suspend fun getTotalFocusSeconds(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)

    @Query("UPDATE focus_sessions SET isSynced = :synced WHERE id = :id")
    suspend fun markAsSynced(id: String, synced: Boolean = true)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}
