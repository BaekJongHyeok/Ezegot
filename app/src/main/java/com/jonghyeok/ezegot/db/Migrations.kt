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
