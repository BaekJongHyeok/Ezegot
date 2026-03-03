package com.jonghyeok.ezegot.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteStationEntity::class, RecentSearchEntity::class, SubwayAlarmEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteStationDao(): FavoriteStationDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun subwayAlarmDao(): SubwayAlarmDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /** Hilt 없이 Widget/Worker에서 주입 없이 다이렉트 접근할 때 사용 */
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ezegot_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
