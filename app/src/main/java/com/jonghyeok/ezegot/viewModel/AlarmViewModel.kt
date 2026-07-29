package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.alarm.SubwayAlarmManager
import com.jonghyeok.ezegot.db.SubwayAlarmDao
import com.jonghyeok.ezegot.db.SubwayAlarmEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 알림 탭. 예약된 도착 알람을 보여주고 지우기만 한다.
 *
 * 알람 생성은 역 상세에서만 하므로 여기서는 조회·삭제뿐이다.
 */
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmDao: SubwayAlarmDao,
    private val alarmManager: SubwayAlarmManager
) : ViewModel() {

    val alarms: StateFlow<List<SubwayAlarmEntity>> = alarmDao.getActiveAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 예약을 취소하고 목록에서 지운다. 예약된 알람 자체도 함께 취소해야 한다 */
    fun cancel(alarm: SubwayAlarmEntity) {
        viewModelScope.launch {
            alarmManager.cancelAlarmByTrain(alarm.trainNo, alarm.stationName)
        }
    }
}
