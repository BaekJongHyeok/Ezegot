package com.jonghyeok.ezegot.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 앱 로컬 DB.
 *
 * 인스턴스 생성은 Hilt(`di/AppModule.provideAppDatabase`)가 전담한다.
 * Worker를 포함해 DB가 필요한 곳은 모두 주입으로 해결한다.
 */
@Database(
    entities = [FavoriteStationEntity::class, RecentSearchEntity::class, SubwayAlarmEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteStationDao(): FavoriteStationDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun subwayAlarmDao(): SubwayAlarmDao
}
