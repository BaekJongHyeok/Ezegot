package com.jonghyeok.ezegot.alarm

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jonghyeok.ezegot.db.SubwayAlarmDao
import com.jonghyeok.ezegot.repository.StationRepository
import com.jonghyeok.ezegot.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class SubwayAlarmWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val stationRepository: StationRepository,
    private val alarmDao: SubwayAlarmDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val alarmId = inputData.getInt("alarmId", -1)
        val stationName = inputData.getString("stationName") ?: return Result.failure()
        val trainNo = inputData.getString("trainNo") ?: return Result.failure()
        val threshold = inputData.getInt("threshold", 180) // 기본 3분 (180초)

        // 최대 1시간 동안 모니터링 (무한 루프 방지)
        val startTime = System.currentTimeMillis()
        val maxDuration = 60 * 60 * 1000L 

        while (System.currentTimeMillis() - startTime < maxDuration) {
            // 알람이 비활성화되었는지 확인
            val alarm = alarmDao.getActiveAlarmByTrain(trainNo, stationName)
            if (alarm == null || !alarm.isActive) {
                return Result.success()
            }

            val arrivals = stationRepository.getRealtimeArrivalInfo(stationName)
            val TargetArrival = arrivals.find { 
                it.trainNumber.trim().replaceFirst("^0+".toRegex(), "") == 
                trainNo.trim().replaceFirst("^0+".toRegex(), "") 
            }

            if (TargetArrival != null) {
                var secondsLeft = TargetArrival.barvlDt.toIntOrNull() ?: -1
                
                // barvlDt가 없으면 메시지에서 시간 추출 시도
                if (secondsLeft <= 0) {
                    val msg = TargetArrival.getFormattedMessage()
                    val minutesMatch = Regex("(\\d+)분 후").find(msg)
                    if (minutesMatch != null) {
                        secondsLeft = (minutesMatch.groupValues[1].toIntOrNull() ?: 0) * 60
                    }
                }

                val msg = TargetArrival.getFormattedMessage()

                // 1. 초 단위 정보 또는 직접적인 메시지("도착", "진입")로 판단
                if ((secondsLeft in 0..threshold) || 
                    msg == "도착" || msg == "진입" || msg == "곧 도착") {
                    
                    showNotification(alarmId, stationName, TargetArrival.trainLineName, msg)
                    alarmDao.deactivateAlarm(alarmId)
                    return Result.success()
                }

                // 2. 적응형 대기 시간 계산
                val nextDelay = when {
                    secondsLeft > threshold + 300 -> 120_000L // 5분 이상 더 남음 -> 2분 대기
                    secondsLeft > threshold + 120 -> 60_000L  // 2분 이상 더 남음 -> 1분 대기
                    else -> 30_000L                            // 그 외 -> 30초 대기
                }
                delay(nextDelay)
            } else {
                // 목록에 없으면 데이터가 아직 안 뜬 것일 수 있음 (최대 5분까지만 더 대기)
                if (System.currentTimeMillis() - startTime > 300_000L && arrivals.isNotEmpty()) {
                    // 기차 목록은 오는데 내 기차만 없는 경우 -> 이미 지나갔을 가능성 큼
                    alarmDao.deactivateAlarm(alarmId)
                    return Result.success()
                }
                delay(30_000L)
            }
        }
        
        alarmDao.deactivateAlarm(alarmId)
        return Result.success()
    }

    private fun showNotification(id: Int, stationName: String, direction: String, status: String) {
        if (!notificationHelper.areNotificationsEnabled()) {
            // Notifications are disabled, do not show.
            // Optionally, log this or inform the user in another way.
            return
        }
        notificationHelper.showArrivalNotification(
            id = id,
            title = "지하철 도착 알림",
            message = "[$stationName] $direction 열차가 $status 입니다.",
            stationName = stationName
        )
    }
}
