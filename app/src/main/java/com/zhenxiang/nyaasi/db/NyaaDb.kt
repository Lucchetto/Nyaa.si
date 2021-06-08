package com.zhenxiang.nyaasi.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NyaaRelease::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class NyaaDb : RoomDatabase() {
    abstract fun nyaaReleasesDao(): NyaaReleaseDao

    companion object {
        @Volatile private var instance: NyaaDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            NyaaDb::class.java, "local_nyaa.db")
            .build()
    }
}
