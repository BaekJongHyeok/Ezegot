package com.jonghyeok.ezegot.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 즐겨찾기 한 건. 역이 아니라 **방향**이 단위다.
 *
 * [direction]은 실시간 API의 `updnLine` 값("상행"/"하행"/"내선"/"외선")이다.
 *
 * 유니크 인덱스가 없으면 같은 방향을 두 번 담을 수 있다. 예전에는 화면이
 * 먼저 확인하는 순서에만 기대고 있어 스키마상으로는 중복이 가능했다.
 */
@Entity(
    tableName = "favorite_stations",
    indices = [Index(value = ["stationName", "lineNumber", "direction"], unique = true)]
)
data class FavoriteStationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationName: String,
    val lineNumber: String,
    val direction: String
)
