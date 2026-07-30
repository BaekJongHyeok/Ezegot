package com.jonghyeok.ezegot.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 즐겨찾기를 역 단위에서 방향 단위로 바꾼다.
 *
 * 기존 1건을 방향 2건으로 늘린다. 상행만 남기면 하행을 쓰던 사용자가
 * 반대 방향 열차를 보게 되는데, 화면상 구분이 없어 틀렸다는 것을 알 수 없다.
 * 늘려두면 필요 없는 쪽을 지우기만 하면 되므로 손실도 오해도 없다.
 *
 * 2호선은 순환선이라 상하행 대신 내선/외선 표기를 쓴다.
 */
/**
 * 즐겨찾기를 다시 역 단위로 되돌린다.
 *
 * 방향 단위로 두니 같은 역의 반대 방향이 별도 항목이 되어 목록에 역 이름이
 * 두 번씩 나왔다. 사용자는 방향이 아니라 역을 담는다.
 *
 * 같은 (역, 노선)의 방향 행들을 한 건으로 접는다. 역 이름만으로 묶으면
 * 환승역에서 서로 다른 노선이 합쳐지므로 노선까지 기준에 넣는다.
 * MIN(id)로 정렬해, 한쪽 방향만 담았다가 나중에 반대쪽을 추가한 역이
 * 목록 끝으로 밀려나지 않게 한다.
 *
 * 방향 정보는 실제로 사라지지만 사용자가 고른 값이 아니다.
 * [MIGRATION_2_3]이 원본 1건을 기계적으로 2건으로 복제해 만든 것이라 잃을 의미가 없다.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE favorite_stations_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                stationName TEXT NOT NULL,
                lineNumber TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO favorite_stations_new (stationName, lineNumber)
            SELECT stationName, lineNumber
            FROM favorite_stations
            GROUP BY stationName, lineNumber
            ORDER BY MIN(id)
            """.trimIndent()
        )
        db.execSQL("DROP TABLE favorite_stations")
        db.execSQL("ALTER TABLE favorite_stations_new RENAME TO favorite_stations")
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_stations_stationName_lineNumber
            ON favorite_stations (stationName, lineNumber)
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE favorite_stations_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                stationName TEXT NOT NULL,
                lineNumber TEXT NOT NULL,
                direction TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO favorite_stations_new (stationName, lineNumber, direction)
            SELECT stationName, lineNumber,
                   CASE WHEN lineNumber LIKE '%2호선%' THEN '내선' ELSE '상행' END
            FROM favorite_stations
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO favorite_stations_new (stationName, lineNumber, direction)
            SELECT stationName, lineNumber,
                   CASE WHEN lineNumber LIKE '%2호선%' THEN '외선' ELSE '하행' END
            FROM favorite_stations
            """.trimIndent()
        )
        db.execSQL("DROP TABLE favorite_stations")
        db.execSQL("ALTER TABLE favorite_stations_new RENAME TO favorite_stations")
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_stations_stationName_lineNumber_direction
            ON favorite_stations (stationName, lineNumber, direction)
            """.trimIndent()
        )
    }
}
