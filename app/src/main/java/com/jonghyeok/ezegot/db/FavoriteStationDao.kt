package com.jonghyeok.ezegot.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {

    @Query("SELECT * FROM favorite_stations ORDER BY id")
    fun getAll(): Flow<List<FavoriteStationEntity>>

    @Query("SELECT * FROM favorite_stations ORDER BY id")
    suspend fun getAllSync(): List<FavoriteStationEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE stationName = :name AND lineNumber = :line")
    suspend fun delete(name: String, line: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationName = :name AND lineNumber = :line)")
    suspend fun exists(name: String, line: String): Boolean

    /** 이 역·노선을 담아뒀는지. 역 상세가 별 상태를 그릴 때 쓴다. */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationName = :name AND lineNumber = :line)")
    fun isFavorite(name: String, line: String): Flow<Boolean>
}
