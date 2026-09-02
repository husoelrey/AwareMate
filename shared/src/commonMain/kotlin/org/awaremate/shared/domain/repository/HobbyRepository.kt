package org.awaremate.shared.domain.repository

import kotlinx.coroutines.flow.Flow
import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCategory

interface HobbyRepository {
    fun getAllHobbies(): Flow<List<Hobby>>
    fun getBookmarkedHobbies(): Flow<List<Hobby>>
    fun getHobbiesByCategory(category: HobbyCategory): Flow<List<Hobby>>
    suspend fun getHobbyById(id: String): Hobby?
    suspend fun initializeDefaultHobbies(): Result<Unit>
    suspend fun toggleBookmark(hobbyId: String, isBookmarked: Boolean): Result<Unit>
    suspend fun completeHobbySession(hobbyId: String): Result<Unit>
}
