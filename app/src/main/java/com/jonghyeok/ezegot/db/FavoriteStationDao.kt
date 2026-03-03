package com.jonghyeok.ezegot.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {

    @Query("SELECT * FROM favorite_stations")
    fun getAll(): Flow<List<FavoriteStationEntity>>

    @Query("SELECT * FROM favorite_stations")
    suspend fun getAllSync(): List<FavoriteStationEntity>

    @Query("SELECT * FROM favorite_stations WHERE stationName = :name AND lineNumber = :line LIMIT 1")
    suspend fun findOne(name: String, line: String): FavoriteStationEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE stationName = :name AND lineNumber = :line")
    suspend fun delete(name: String, line: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationName = :name AND lineNumber = :line)")
    suspend fun exists(name: String, line: String): Boolean
}
