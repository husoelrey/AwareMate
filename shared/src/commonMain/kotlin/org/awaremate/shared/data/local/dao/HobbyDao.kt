package org.awaremate.shared.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.data.local.entity.HobbyEntity

@Dao
interface HobbyDao {
    @Query("SELECT * FROM hobbies ORDER BY title ASC")
    fun getAllHobbiesFlow(): Flow<List<HobbyEntity>>

    @Query("SELECT * FROM hobbies WHERE isBookmarked = 1 ORDER BY title ASC")
    fun getBookmarkedHobbiesFlow(): Flow<List<HobbyEntity>>

    @Query("SELECT * FROM hobbies WHERE category = :category ORDER BY title ASC")
    fun getHobbiesByCategoryFlow(category: String): Flow<List<HobbyEntity>>

    @Query("SELECT * FROM hobbies WHERE id = :id LIMIT 1")
    suspend fun getHobbyById(id: String): HobbyEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultHobbies(hobbies: List<HobbyEntity>)

    @Query("UPDATE hobbies SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmark(id: String, bookmarked: Boolean)

    @Query("UPDATE hobbies SET sessionsCompleted = sessionsCompleted + 1, lastCompletedEpochMs = :timestamp WHERE id = :id")
    suspend fun incrementSessionCount(id: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM hobbies")
    suspend fun getHobbyCount(): Int
}
