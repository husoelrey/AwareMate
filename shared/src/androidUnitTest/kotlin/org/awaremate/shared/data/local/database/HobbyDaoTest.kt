package org.awaremate.shared.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.local.entity.HobbyEntity
import org.awaremate.shared.data.local.entity.SelfDiscoveryPromptEntity
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class HobbyDaoTest {

    private lateinit var database: AwareMateDatabase

    @BeforeTest
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AwareMateDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun testHobbyDaoInsertAndQueryByCategory() = runTest {
        val hobbyDao = database.hobbyDao()
        val hobby1 = HobbyEntity(
            id = "h-1",
            title = "Watercolor Doodling",
            category = "CREATIVE_ARTS",
            description = "Gentle paint doodling",
            beginnerTip = "Use water",
            estimatedDurationMinutes = 20,
            energyLevel = "GENTLE"
        )
        val hobby2 = HobbyEntity(
            id = "h-2",
            title = "Sensory Nature Walk",
            category = "NATURE_OUTDOORS",
            description = "Mindful outdoor steps",
            beginnerTip = "Listen to birds",
            estimatedDurationMinutes = 30,
            energyLevel = "ACTIVE"
        )

        hobbyDao.insertDefaultHobbies(listOf(hobby1, hobby2))

        val all = hobbyDao.getAllHobbiesFlow().first()
        assertEquals(2, all.size)

        val creative = hobbyDao.getHobbiesByCategoryFlow("CREATIVE_ARTS").first()
        assertEquals(1, creative.size)
        assertEquals("h-1", creative[0].id)
    }

    @Test
    fun testHobbyDaoBookmarkAndIncrementSession() = runTest {
        val hobbyDao = database.hobbyDao()
        val hobby = HobbyEntity(
            id = "h-3",
            title = "Origami Folding",
            category = "HANDS_ON_CRAFT",
            description = "Fold crisp paper figures",
            beginnerTip = "Crease carefully",
            estimatedDurationMinutes = 15,
            energyLevel = "GENTLE",
            isBookmarked = false,
            sessionsCompleted = 0
        )

        hobbyDao.insertDefaultHobbies(listOf(hobby))

        hobbyDao.setBookmark("h-3", true)
        val bookmarked = hobbyDao.getBookmarkedHobbiesFlow().first()
        assertEquals(1, bookmarked.size)
        assertTrue(bookmarked[0].isBookmarked)

        hobbyDao.incrementSessionCount("h-3", 1725286000000L)
        val updated = hobbyDao.getHobbyById("h-3")
        assertNotNull(updated)
        assertEquals(1, updated.sessionsCompleted)
        assertEquals(1725286000000L, updated.lastCompletedEpochMs)
    }

    @Test
    fun testSelfDiscoveryPromptDaoInsertAndReflection() = runTest {
        val promptDao = database.selfDiscoveryPromptDao()
        val prompt = SelfDiscoveryPromptEntity(
            id = "sd-1",
            category = "Pocket Reflex",
            question = "What was your mind doing before reaching for phone?",
            curiosityHint = "Notice stimulation seeking",
            isAcknowledged = false,
            userReflection = null
        )

        promptDao.insertDefaultPrompts(listOf(prompt))

        val all = promptDao.getAllPromptsFlow().first()
        assertEquals(1, all.size)
        assertEquals(false, all[0].isAcknowledged)

        promptDao.savePromptReflection("sd-1", "I noticed a brief boredom pause", 1725287000000L)

        val updated = promptDao.getPromptById("sd-1")
        assertNotNull(updated)
        assertTrue(updated.isAcknowledged)
        assertEquals("I noticed a brief boredom pause", updated.userReflection)
        assertEquals(1725287000000L, updated.lastAnsweredEpochMs)
    }
}
