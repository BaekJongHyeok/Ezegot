package com.jonghyeok.ezegot.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationName: String,
    val lineNumber: String,
    val searchedAt: Long = System.currentTimeMillis()
)
