package org.awaremate.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.data.local.entity.SelfDiscoveryPromptEntity

@Dao
interface SelfDiscoveryPromptDao {
    @Query("SELECT * FROM self_discovery_prompts ORDER BY id ASC")
    fun getAllPromptsFlow(): Flow<List<SelfDiscoveryPromptEntity>>

    @Query("SELECT * FROM self_discovery_prompts WHERE id = :id LIMIT 1")
    suspend fun getPromptById(id: String): SelfDiscoveryPromptEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultPrompts(prompts: List<SelfDiscoveryPromptEntity>)

    @Query("UPDATE self_discovery_prompts SET isAcknowledged = 1, userReflection = :reflection, lastAnsweredEpochMs = :timestamp WHERE id = :id")
    suspend fun savePromptReflection(id: String, reflection: String?, timestamp: Long)

    @Query("SELECT COUNT(*) FROM self_discovery_prompts")
    suspend fun getPromptCount(): Int
}
