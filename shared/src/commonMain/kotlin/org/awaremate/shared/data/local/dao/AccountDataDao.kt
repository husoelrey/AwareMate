package org.awaremate.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class AccountDataDao {

    @Transaction
    open suspend fun clearAllAccountData() {
        clearUsers()
        clearCompanions()
        clearMoodEntries()
        clearFocusSessions()
        clearDailyChallenges()
        clearScreenTimeSnapshots()
        clearHobbies()
        clearSelfDiscoveryPrompts()
    }

    @Query("DELETE FROM users")
    protected abstract suspend fun clearUsers()

    @Query("DELETE FROM companions")
    protected abstract suspend fun clearCompanions()

    @Query("DELETE FROM mood_entries")
    protected abstract suspend fun clearMoodEntries()

    @Query("DELETE FROM focus_sessions")
    protected abstract suspend fun clearFocusSessions()

    @Query("DELETE FROM daily_challenges")
    protected abstract suspend fun clearDailyChallenges()

    @Query("DELETE FROM screen_time_snapshots")
    protected abstract suspend fun clearScreenTimeSnapshots()

    @Query("DELETE FROM hobbies")
    protected abstract suspend fun clearHobbies()

    @Query("DELETE FROM self_discovery_prompts")
    protected abstract suspend fun clearSelfDiscoveryPrompts()
}
