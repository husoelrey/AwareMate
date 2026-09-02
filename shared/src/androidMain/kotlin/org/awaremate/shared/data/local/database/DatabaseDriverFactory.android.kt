package org.awaremate.shared.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun getAndroidDatabaseBuilder(context: Context): RoomDatabase.Builder<AwareMateDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("awaremate.db")
    return Room.databaseBuilder<AwareMateDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
}

fun createAndroidInMemoryDatabase(context: Context): AwareMateDatabase {
    return Room.inMemoryDatabaseBuilder<AwareMateDatabase>(
        context = context.applicationContext
    ).setDriver(BundledSQLiteDriver()).build()
}
