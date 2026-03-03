package com.jonghyeok.ezegot.alarm

import android.content.Context
import androidx.work.*
import com.jonghyeok.ezegot.db.SubwayAlarmDao
import com.jonghyeok.ezegot.db.SubwayAlarmEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubwayAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmDao: SubwayAlarmDao
) {
    private val workManager = WorkManager.getInstance(context)

    suspend fun scheduleAlarm(
        stationName: String,
        lineNumber: String,
        trainNo: String,
        destination: String,
        direction: String,
        thresholdSeconds: Int,
        arrivalSeconds: Int // 현재 열차의 남은 도착 시간(초)
    ): Long {
        // 1. 기존에 해당 열차에 대한 활성 알람이 있는지 확인
        val existing = alarmDao.getActiveAlarmByTrain(trainNo, stationName)
        if (existing != null) return existing.id.toLong()

        // 2. DB 저장
        val alarm = SubwayAlarmEntity(
            stationName = stationName,
            lineNumber = lineNumber,
            trainNo = trainNo,
            destination = destination,
            direction = direction,
            alarmTimeThreshold = thresholdSeconds
        )
        val alarmId = alarmDao.insertAlarm(alarm).toInt()

        // 3. 지연 시간 계산 (목표 시간 1분 전까지 대기)
        // 예: 10분 후 도착 예정, 3분 전 알림 설정 -> 10 - 3 - 1 = 6분 대기 후 체크 시작
        val bufferSeconds = 60 
        val initialDelay = (arrivalSeconds - thresholdSeconds - bufferSeconds).coerceAtLeast(0)

        // 4. WorkManager 예약
        val inputData = workDataOf(
            "alarmId" to alarmId,
            "stationName" to stationName,
            "trainNo" to trainNo,
            "threshold" to thresholdSeconds
        )

        val workRequest = OneTimeWorkRequestBuilder<SubwayAlarmWorker>()
            .setInputData(inputData)
            .setInitialDelay(initialDelay.toLong(), TimeUnit.SECONDS)
            .addTag("subway_alarm_${alarmId}")
            .addTag("train_${trainNo}")
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "subway_alarm_${stationName}_${trainNo}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        return alarmId.toLong()
    }

    suspend fun cancelAlarm(alarmId: Int) {
        alarmDao.deactivateAlarm(alarmId)
        workManager.cancelAllWorkByTag("subway_alarm_$alarmId")
    }

    suspend fun cancelAlarmByTrain(trainNo: String, stationName: String) {
        val alarm = alarmDao.getActiveAlarmByTrain(trainNo, stationName)
        if (alarm != null) {
            cancelAlarm(alarm.id)
        }
    }
}
