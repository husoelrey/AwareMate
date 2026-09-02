package org.awaremate.shared.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.awaremate.shared.data.local.dao.HobbyDao
import org.awaremate.shared.data.local.entity.HobbyEntity
import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCatalog
import org.awaremate.shared.domain.model.HobbyCategory
import org.awaremate.shared.domain.repository.HobbyRepository

class HobbyRepositoryImpl(
    private val hobbyDao: HobbyDao
) : HobbyRepository {

    override fun getAllHobbies(): Flow<List<Hobby>> {
        return hobbyDao.getAllHobbiesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBookmarkedHobbies(): Flow<List<Hobby>> {
        return hobbyDao.getBookmarkedHobbiesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHobbiesByCategory(category: HobbyCategory): Flow<List<Hobby>> {
        return hobbyDao.getHobbiesByCategoryFlow(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHobbyById(id: String): Hobby? {
        return hobbyDao.getHobbyById(id)?.toDomain()
    }

    override suspend fun initializeDefaultHobbies(): Result<Unit> = runCatching {
        val count = hobbyDao.getHobbyCount()
        if (count == 0) {
            val defaultEntities = HobbyCatalog.defaultHobbies.map { HobbyEntity.fromDomain(it) }
            hobbyDao.insertDefaultHobbies(defaultEntities)
        }
    }

    override suspend fun toggleBookmark(hobbyId: String, isBookmarked: Boolean): Result<Unit> = runCatching {
        hobbyDao.setBookmark(hobbyId, isBookmarked)
    }

    override suspend fun completeHobbySession(hobbyId: String): Result<Unit> = runCatching {
        val now = Clock.System.now().toEpochMilliseconds()
        hobbyDao.incrementSessionCount(hobbyId, now)
    }
}
