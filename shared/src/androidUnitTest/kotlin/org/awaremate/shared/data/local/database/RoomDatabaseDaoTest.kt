package org.awaremate.shared.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.awaremate.shared.data.local.entity.CompanionEntity
import org.awaremate.shared.data.local.entity.DailyChallengeEntity
import org.awaremate.shared.data.local.entity.FocusSessionEntity
import org.awaremate.shared.data.local.entity.MoodEntryEntity
import org.awaremate.shared.data.local.entity.UserEntity
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class RoomDatabaseDaoTest {

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
    fun testUserDaoInsertAndRetrieve() = runTest {
        val userDao = database.userDao()
        val user = UserEntity(
            id = "u-room-1",
            displayName = "Taylor",
            email = "taylor@awaremate.org",
            isAnonymous = false,
            createdAtEpochMs = 1725280000000L,
            lastActiveEpochMs = 1725281000000L
        )

        userDao.insertUser(user)

        val retrieved = userDao.getUserById("u-room-1")
        assertNotNull(retrieved)
        assertEquals("Taylor", retrieved.displayName)
        assertEquals("taylor@awaremate.org", retrieved.email)

        val flowUser = userDao.getCurrentUserFlow().first()
        assertEquals("u-room-1", flowUser?.id)
    }

    @Test
    fun testCompanionDaoInsertAndRetrieve() = runTest {
        val companionDao = database.companionDao()
        val companion = CompanionEntity(
            id = "primary",
            name = "Verdant",
            stage = "SPROUT",
            emotion = "CHEERFUL",
            experiencePoints = 150,
            momentumScore = 2.0,
            happinessXp = 50,
            energyXp = 50,
            wisdomXp = 30,
            creativityXp = 20,
            lastUpdatedEpochMs = 1725282000000L
        )

        companionDao.insertCompanion(companion)

        val retrieved = companionDao.getCompanionById("primary")
        assertNotNull(retrieved)
        assertEquals("Verdant", retrieved.name)
        assertEquals("SPROUT", retrieved.stage)
        assertEquals(150, retrieved.experiencePoints)

        val domainCompanion = retrieved.toDomain()
        assertEquals("Verdant", domainCompanion.name)
    }

    @Test
    fun testMoodEntryDaoInsertQueryRangeAndSync() = runTest {
        val moodDao = database.moodEntryDao()
        val entry1 = MoodEntryEntity(
            id = "mood-1",
            userId = "u-1",
            timestampEpochMs = 1000L,
            emoji = "🌱",
            moodScore = 4,
            energyLevel = 3,
            note = "Morning check-in",
            tags = listOf("peaceful", "mindful"),
            isSynced = false
        )
        val entry2 = MoodEntryEntity(
            id = "mood-2",
            userId = "u-1",
            timestampEpochMs = 2000L,
            emoji = "🌸",
            moodScore = 5,
            energyLevel = 5,
            note = "Great focus session",
            tags = listOf("joy", "progress"),
            isSynced = false
        )

        moodDao.insertMoodEntry(entry1)
        moodDao.insertMoodEntry(entry2)

        val allEntries = moodDao.getAllMoodEntriesFlow().first()
        assertEquals(2, allEntries.size)

        val unsynced = moodDao.getUnsyncedEntries()
        assertEquals(2, unsynced.size)

        moodDao.markAsSynced("mood-1", true)
        val remainingUnsynced = moodDao.getUnsyncedEntries()
        assertEquals(1, remainingUnsynced.size)
        assertEquals("mood-2", remainingUnsynced[0].id)

        val rangeEntries = moodDao.getMoodEntriesForRange(1500L, 2500L).first()
        assertEquals(1, rangeEntries.size)
        assertEquals("mood-2", rangeEntries[0].id)
    }

    @Test
    fun testFocusSessionDaoInsertAndTotalSeconds() = runTest {
        val focusDao = database.focusSessionDao()
        val session1 = FocusSessionEntity(
            id = "fs-1",
            userId = "u-1",
            startTimeEpochMs = 1000L,
            durationSeconds = 1800, // 30 min
            category = "DEEP_WORK",
            earnedXp = 30,
            completed = true,
            isSynced = false
        )
        val session2 = FocusSessionEntity(
            id = "fs-2",
            userId = "u-1",
            startTimeEpochMs = 5000L,
            durationSeconds = 1200, // 20 min
            category = "STUDY",
            earnedXp = 20,
            completed = true,
            isSynced = false
        )

        focusDao.insertSession(session1)
        focusDao.insertSession(session2)

        val totalSeconds = focusDao.getTotalFocusSeconds()
        assertEquals(3000L, totalSeconds) // 50 min total
    }

    @Test
    fun testDailyChallengeDaoInsertAndComplete() = runTest {
        val challengeDao = database.dailyChallengeDao()
        val challenge = DailyChallengeEntity(
            id = "dc-10",
            userId = "u-1",
            title = "Hydrate Mindfully",
            description = "Drink water before looking at screen",
            category = "ENERGY",
            xpReward = 15,
            dateString = "2026-09-02",
            completed = false,
            isSynced = false
        )

        challengeDao.insertChallenge(challenge)

        val challenges = challengeDao.getChallengesForDateFlow("2026-09-02").first()
        assertEquals(1, challenges.size)
        assertEquals(false, challenges[0].completed)

        challengeDao.markAsCompleted("dc-10", 1725285000000L)

        val updated = challengeDao.getChallengesForDateFlow("2026-09-02").first()
        assertTrue(updated[0].completed)
        assertEquals(1725285000000L, updated[0].completedAtEpochMs)
    }

    @Test
    fun accountDataDaoClearsPersistedAccountRows() = runTest {
        database.userDao().insertUser(
            UserEntity(
                id = "delete-me",
                displayName = "Deletion audit",
                isAnonymous = true
            )
        )
        database.moodEntryDao().insertMoodEntry(
            MoodEntryEntity(
                id = "delete-mood",
                userId = "delete-me",
                timestampEpochMs = 1_725_280_000_000L,
                emoji = "🌱",
                moodScore = 4,
                energyLevel = 3
            )
        )

        database.accountDataDao().clearAllAccountData()

        assertEquals(null, database.userDao().getUserById("delete-me"))
        assertTrue(database.moodEntryDao().getAllMoodEntriesFlow().first().isEmpty())
    }
}
