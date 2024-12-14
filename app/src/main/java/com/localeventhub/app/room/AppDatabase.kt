package com.localeventhub.app.room

import androidx.room.Database
import androidx.room.RoomDatabase

//@Database(entities = [], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}