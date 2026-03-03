package com.jonghyeok.ezegot.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stations")
data class FavoriteStationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationName: String,
    val lineNumber: String
)
