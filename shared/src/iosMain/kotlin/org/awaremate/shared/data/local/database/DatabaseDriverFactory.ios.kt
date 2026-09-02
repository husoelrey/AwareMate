package org.awaremate.shared.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

fun getIosDatabaseBuilder(): RoomDatabase.Builder<AwareMateDatabase> {
    val dbFilePath = NSHomeDirectory() + "/awaremate.db"
    return Room.databaseBuilder<AwareMateDatabase>(
        name = dbFilePath,
        factory = { AwareMateDatabaseConstructor.initialize() }
    ).setDriver(BundledSQLiteDriver())
}
