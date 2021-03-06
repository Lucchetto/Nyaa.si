package com.zhenxiang.nyaa.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zhenxiang.nyaa.releasetracker.SubscribedTracker
import com.zhenxiang.nyaa.releasetracker.SubscribedTrackerDao

@Database(entities = [NyaaReleasePreview::class, NyaaReleaseDetails::class, NyaaSearchHistoryItem::class,
    ViewedNyaaRelease::class, SavedNyaaRelease::class, SubscribedTracker::class], version = 3,
)
@TypeConverters(DbTypeConverters::class)
abstract class NyaaDb : RoomDatabase() {
    abstract fun nyaaReleasesPreviewDao(): NyaaReleasePreviewDao
    abstract fun nyaaReleasesDetailsDao(): NyaaReleaseDetailsDao
    abstract fun nyaaSearchHistoryDao(): NyaaSearchHistoryDao
    abstract fun viewedNyaaReleasesDao(): ViewedNyaaReleaseDao
    abstract fun savedNyaaReleasesDao(): SavedNyaaReleaseDao

    abstract fun subscribedTrackersDao(): SubscribedTrackerDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS `new_nyaaReleasePreview` (`number` INTEGER NOT NULL, `name` TEXT NOT NULL, `magnet` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `seeders` INTEGER NOT NULL, `leechers` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `releaseSize` TEXT NOT NULL, `dataSource` INTEGER NOT NULL, `categoryId` TEXT NOT NULL, PRIMARY KEY(`number`, `dataSource`));
                """)
                database.execSQL("""INSERT INTO new_nyaaReleasePreview(dataSource, number, name, magnet, timestamp, seeders, leechers, completed, categoryId, releaseSize) SELECT 0, id, name, magnet, timestamp, seeders, leechers, completed, category, releaseSize FROM nyaaReleasePreview;""")
                database.execSQL("""DROP TABLE nyaaReleasePreview;""")
                database.execSQL("""ALTER TABLE `new_nyaaReleasePreview` RENAME TO `nyaaReleasePreview`;""")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `new_nyaaReleaseDetails` (`user` TEXT, `hash` TEXT NOT NULL, `descriptionMarkdown` TEXT NOT NULL, `parent_number` INTEGER NOT NULL, `parent_dataSource` INTEGER NOT NULL, PRIMARY KEY(`parent_number`, `parent_dataSource`), FOREIGN KEY(`parent_number`, `parent_dataSource`) REFERENCES `NyaaReleasePreview`(`number`, `dataSource`) ON UPDATE CASCADE ON DELETE CASCADE )""")
                database.execSQL("""INSERT INTO new_nyaaReleaseDetails(parent_dataSource, parent_number, user, hash, descriptionMarkdown) SELECT 0, parentId, user, hash, descriptionMarkdown FROM nyaaReleaseDetails;""")
                database.execSQL("DROP TABLE nyaaReleaseDetails;")
                database.execSQL("ALTER TABLE `new_nyaaReleaseDetails` RENAME TO `nyaaReleaseDetails`;")
                database.execSQL("CREATE TABLE IF NOT EXISTS `new_viewedNyaaRelease` (`viewedTimestamp` INTEGER NOT NULL, `parent_number` INTEGER NOT NULL, `parent_dataSource` INTEGER NOT NULL, PRIMARY KEY(`parent_number`, `parent_dataSource`), FOREIGN KEY(`parent_number`, `parent_dataSource`) REFERENCES `NyaaReleasePreview`(`number`, `dataSource`) ON UPDATE CASCADE ON DELETE RESTRICT )")
                database.execSQL("INSERT INTO new_viewedNyaaRelease(parent_dataSource, parent_number, viewedTimestamp) SELECT 0, releaseId, timestamp FROM viewedNyaaRelease")
                database.execSQL("DROP TABLE viewedNyaaRelease;")
                database.execSQL("ALTER TABLE `new_viewedNyaaRelease` RENAME TO `viewedNyaaRelease`;")
                database.execSQL("CREATE TABLE IF NOT EXISTS `new_savedNyaaRelease` (`savedTimestamp` INTEGER NOT NULL, `parent_number` INTEGER NOT NULL, `parent_dataSource` INTEGER NOT NULL, PRIMARY KEY(`parent_number`, `parent_dataSource`), FOREIGN KEY(`parent_number`, `parent_dataSource`) REFERENCES `NyaaReleasePreview`(`number`, `dataSource`) ON UPDATE CASCADE ON DELETE RESTRICT )")
                database.execSQL("INSERT INTO new_savedNyaaRelease(parent_dataSource, parent_number, savedTimestamp) SELECT 0, releaseId, timestamp FROM savedNyaaRelease")
                database.execSQL("DROP TABLE savedNyaaRelease;")
                database.execSQL("ALTER TABLE `new_savedNyaaRelease` RENAME TO `savedNyaaRelease`;")
                database.execSQL("CREATE TABLE IF NOT EXISTS `new_subscribedTracker` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT, `searchQuery` TEXT, `latestReleaseTimestamp` INTEGER NOT NULL, `hasPreviousReleases` INTEGER NOT NULL, `createdTimestamp` INTEGER NOT NULL, `newReleasesCount` INTEGER NOT NULL, `dataSource` INTEGER NOT NULL, `categoryId` TEXT NOT NULL)")
                database.execSQL("INSERT INTO new_subscribedTracker(dataSource, id, username, searchQuery, categoryId, latestReleaseTimestamp, hasPreviousReleases, createdTimestamp, newReleasesCount) SELECT 0, id, username, searchQuery, category, latestReleaseTimestamp, hasPreviousReleases, createdTimestamp, newReleasesCount FROM subscribedTracker")
                database.execSQL("DROP TABLE subscribedTracker;")
                database.execSQL("ALTER TABLE `new_subscribedTracker` RENAME TO `subscribedTracker`;")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `subscribedTracker` ADD COLUMN name TEXT;")
            }
        }


        @Volatile private var instance: NyaaDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            NyaaDb::class.java, "local_nyaa.db")
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .build()
    }
}
