package com.jonghyeok.ezegot.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {

    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT 20")
    fun getAll(): Flow<List<RecentSearchEntity>>

    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT 20")
    suspend fun getAllOnce(): List<RecentSearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE stationName = :name AND lineNumber = :line")
    suspend fun deleteOne(name: String, line: String)

    @Query("DELETE FROM recent_searches")
    suspend fun deleteAll()
}
