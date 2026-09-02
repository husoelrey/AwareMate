package org.awaremate.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.data.local.entity.DailyChallengeEntity

@Dao
interface DailyChallengeDao {
    @Query("SELECT * FROM daily_challenges WHERE dateString = :dateString ORDER BY id ASC")
    fun getChallengesForDateFlow(dateString: String): Flow<List<DailyChallengeEntity>>

    @Query("SELECT * FROM daily_challenges WHERE isSynced = 0")
    suspend fun getUnsyncedChallenges(): List<DailyChallengeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<DailyChallengeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: DailyChallengeEntity)

    @Query("UPDATE daily_challenges SET completed = 1, completedAtEpochMs = :completedAt, isSynced = 0 WHERE id = :id")
    suspend fun markAsCompleted(id: String, completedAt: Long)

    @Query("UPDATE daily_challenges SET isSynced = :synced WHERE id = :id")
    suspend fun markAsSynced(id: String, synced: Boolean = true)

    @Query("DELETE FROM daily_challenges")
    suspend fun clearAllChallenges()
}
