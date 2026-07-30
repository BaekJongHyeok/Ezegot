package com.jonghyeok.ezegot.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 즐겨찾기 한 건. **역**이 단위다.
 *
 * 한동안 방향(상행/하행)을 저장 단위로 두었는데, 같은 역의 반대 방향이 별도
 * 항목이 되어 목록에 역 이름이 두 번씩 나왔다. 사용자는 방향이 아니라 역을
 * 담는다. 방향은 화면에서 한 항목 안에 나란히 보여주면 된다.
 *
 * 유니크 인덱스가 없으면 같은 역을 두 번 담을 수 있다.
 * 환승역은 노선이 다르면 다른 항목이므로 노선까지 포함한다.
 */
@Entity(
    tableName = "favorite_stations",
    indices = [Index(value = ["stationName", "lineNumber"], unique = true)]
)
data class FavoriteStationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationName: String,
    val lineNumber: String
)
