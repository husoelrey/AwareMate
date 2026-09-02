package org.awaremate.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.data.local.entity.CompanionEntity

@Dao
interface CompanionDao {
    @Query("SELECT * FROM companions WHERE id = :id LIMIT 1")
    fun getCompanionFlow(id: String = "primary"): Flow<CompanionEntity?>

    @Query("SELECT * FROM companions WHERE id = :id LIMIT 1")
    suspend fun getCompanionById(id: String = "primary"): CompanionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanion(companion: CompanionEntity)

    @Query("DELETE FROM companions")
    suspend fun clearAllCompanions()
}
