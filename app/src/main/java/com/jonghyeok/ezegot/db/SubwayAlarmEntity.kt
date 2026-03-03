package com.jonghyeok.ezegot.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 활성화된 지하철 도착 알람 정보.
 */
@Entity(tableName = "subway_alarms")
data class SubwayAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationName: String,
    val lineNumber: String,
    val trainNo: String,
    val destination: String,
    val direction: String,
    val alarmTimeThreshold: Int, // 알림이 울려야 하는 기준 시간 (초 단위, 예: 180)
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface SubwayAlarmDao {
    @Query("SELECT * FROM subway_alarms WHERE isActive = 1")
    fun getActiveAlarms(): Flow<List<SubwayAlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: SubwayAlarmEntity): Long

    @Query("UPDATE subway_alarms SET isActive = 0 WHERE id = :alarmId")
    suspend fun deactivateAlarm(alarmId: Int)

    @Query("DELETE FROM subway_alarms WHERE id = :alarmId")
    suspend fun deleteAlarm(alarmId: Int)

    @Query("SELECT * FROM subway_alarms WHERE trainNo = :trainNo AND stationName = :stationName AND isActive = 1 LIMIT 1")
    suspend fun getActiveAlarmByTrain(trainNo: String, stationName: String): SubwayAlarmEntity?
}
