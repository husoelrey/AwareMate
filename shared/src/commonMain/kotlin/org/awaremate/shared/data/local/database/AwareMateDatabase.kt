package org.awaremate.shared.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.awaremate.shared.data.local.dao.CompanionDao
import org.awaremate.shared.data.local.dao.DailyChallengeDao
import org.awaremate.shared.data.local.dao.FocusSessionDao
import org.awaremate.shared.data.local.dao.MoodEntryDao
import org.awaremate.shared.data.local.dao.ScreenTimeDao
import org.awaremate.shared.data.local.dao.UserDao
import org.awaremate.shared.data.local.entity.CompanionEntity
import org.awaremate.shared.data.local.entity.DailyChallengeEntity
import org.awaremate.shared.data.local.entity.FocusSessionEntity
import org.awaremate.shared.data.local.entity.MoodEntryEntity
import org.awaremate.shared.data.local.entity.RoomTypeConverters
import org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity
import org.awaremate.shared.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CompanionEntity::class,
        MoodEntryEntity::class,
        FocusSessionEntity::class,
        DailyChallengeEntity::class,
        ScreenTimeSnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
@ConstructedBy(AwareMateDatabaseConstructor::class)
abstract class AwareMateDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun companionDao(): CompanionDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun screenTimeDao(): ScreenTimeDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AwareMateDatabaseConstructor : RoomDatabaseConstructor<AwareMateDatabase> {
    override fun initialize(): AwareMateDatabase
}
