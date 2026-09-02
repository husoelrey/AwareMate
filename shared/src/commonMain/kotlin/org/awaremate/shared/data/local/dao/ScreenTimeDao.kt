package org.awaremate.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity

@Dao
interface ScreenTimeDao {
    @Query("SELECT * FROM screen_time_snapshots WHERE dateString = :dateString")
    fun getSnapshotFlow(dateString: String): Flow<ScreenTimeSnapshotEntity?>

    @Query("SELECT * FROM screen_time_snapshots WHERE dateString = :dateString")
    suspend fun getSnapshot(dateString: String): ScreenTimeSnapshotEntity?

    @Query("SELECT * FROM screen_time_snapshots WHERE dateString IN (:dateStrings) ORDER BY dateString ASC")
    suspend fun getSnapshots(dateStrings: List<String>): List<ScreenTimeSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: ScreenTimeSnapshotEntity)

    @Query("DELETE FROM screen_time_snapshots WHERE dateString = :dateString")
    suspend fun deleteSnapshot(dateString: String)
}
