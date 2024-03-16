package com.meganov.passwordmanager.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Site::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
}
