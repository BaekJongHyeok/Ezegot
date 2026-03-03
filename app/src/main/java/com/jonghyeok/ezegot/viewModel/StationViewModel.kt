package com.jonghyeok.ezegot.viewModel

import android.location.Geocoder
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.jonghyeok.ezegot.MyApplication
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.repository.FavoriteRepository
import com.jonghyeok.ezegot.repository.StationRepository
import com.jonghyeok.ezegot.alarm.SubwayAlarmManager
import com.jonghyeok.ezegot.db.SubwayAlarmDao
import com.jonghyeok.ezegot.db.SubwayAlarmEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StationViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val favoriteRepository: FavoriteRepository,
    private val alarmManager: SubwayAlarmManager,
    private val alarmDao: SubwayAlarmDao
) : BaseViewModel() {

    private val _activeAlarms = MutableStateFlow<List<SubwayAlarmEntity>>(emptyList())
    val activeAlarms: StateFlow<List<SubwayAlarmEntity>> = _activeAlarms.asStateFlow()

    init {
        viewModelScope.launch {
            alarmDao.getActiveAlarms().collect {
                _activeAlarms.value = it
            }
        }
    }

    fun setAlarm(arrival: RealtimeArrival, thresholdMinutes: Int = 3) {
        val station = _stationInfo.value ?: return
        
        // barvlDt가 없어도 getFormattedMessage()에서 추출한 대략적인 시간을 사용
        var arrivalSeconds = arrival.barvlDt.toIntOrNull() ?: 0
        if (arrivalSeconds <= 0) {
            val msg = arrival.getFormattedMessage()
            val minutesMatch = Regex("(\\d+)분 후").find(msg)
            if (minutesMatch != null) {
                arrivalSeconds = (minutesMatch.groupValues[1].toIntOrNull() ?: 0) * 60
            }
        }

        viewModelScope.launch {
            alarmManager.scheduleAlarm(
                stationName = station.stationName,
                lineNumber = arrival.subwayId,
                trainNo = arrival.trainNumber,
                destination = arrival.bstatnNm,
                direction = arrival.trainLineName,
                thresholdSeconds = thresholdMinutes * 60,
                arrivalSeconds = arrivalSeconds
            )
        }
    }

    fun cancelAlarm(trainNo: String, stationName: String) {
        viewModelScope.launch {
            alarmManager.cancelAlarmByTrain(trainNo, stationName)
        }
    }

    private val _stationInfo = MutableStateFlow<BasicStationInfo?>(null)
    val stationInfo: StateFlow<BasicStationInfo?> = _stationInfo.asStateFlow()

    private val _arrivalInfo = MutableStateFlow<List<RealtimeArrival>>(emptyList())
    val arrivalInfo: StateFlow<List<RealtimeArrival>> = _arrivalInfo.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isNotification = MutableStateFlow(false)
    val isNotification: StateFlow<Boolean> = _isNotification.asStateFlow()

    private val _stationLocation = MutableStateFlow<StationInfoResponse?>(null)
    val stationLocation: StateFlow<StationInfoResponse?> = _stationLocation.asStateFlow()

    // ─── 고급 기능 상태 관리 ───
    private val _timeTable = MutableStateFlow<Pair<com.jonghyeok.ezegot.api.TimeTableResponse?, com.jonghyeok.ezegot.api.TimeTableResponse?>?>(null)
    val timeTable: StateFlow<Pair<com.jonghyeok.ezegot.api.TimeTableResponse?, com.jonghyeok.ezegot.api.TimeTableResponse?>?> = _timeTable.asStateFlow()

    private val _transferInfo = MutableStateFlow<com.jonghyeok.ezegot.api.TransferInfoResponse?>(null)
    val transferInfo: StateFlow<com.jonghyeok.ezegot.api.TransferInfoResponse?> = _transferInfo.asStateFlow()

    private val _facilityInfo = MutableStateFlow<com.jonghyeok.ezegot.api.FacilityInfoResponse?>(null)
    val facilityInfo: StateFlow<com.jonghyeok.ezegot.api.FacilityInfoResponse?> = _facilityInfo.asStateFlow()

    fun loadStationInfo(stationName: String, line: String) {
        val info = BasicStationInfo(stationName, line)
        _stationInfo.value = info
        viewModelScope.launch {
            _isFavorite.value = favoriteRepository.isFavorite(info)
        }
    }

    fun loadArrivalInfo(stationName: String) {
        viewModelScope.launch {
            _arrivalInfo.value = stationRepository.getRealtimeArrivalInfo(stationName)
        }
    }

    fun toggleFavorite() {
        val station = _stationInfo.value ?: return
        viewModelScope.launch {
            if (_isFavorite.value) {
                favoriteRepository.removeFavorite(station)
            } else {
                favoriteRepository.addFavorite(station)
            }
            _isFavorite.value = !_isFavorite.value
        }
    }

    fun toggleNotification() {
        _isNotification.value = !_isNotification.value
    }

    fun loadStationLocation(stationName: String, defaultLocation: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            val locations = stationRepository.getStationsLocationList()
            var target = locations.find { it.stationName == stationName }

            if (target != null) {
                val address = stationRepository.getAddress(target.latitude, target.longitude)
                target = target.copy(address = address)
            }

            val result = target ?: StationInfoResponse(
                stationId = "default",
                stationName = "현재 위치",
                lineName = "N/A",
                longitude = defaultLocation.longitude,
                latitude = defaultLocation.latitude,
                address = stationRepository.getAddress(defaultLocation.latitude, defaultLocation.longitude)
            )

            withContext(Dispatchers.Main) { _stationLocation.value = result }
        }
    }

    // ─── 고급 기능 데이터 로드 ───
    fun loadAdvancedStationInfo(stationName: String, lineNumber: String) {
        viewModelScope.launch {
            // 병렬 수행 트리거 (에러가 나도 UI가 죽지 않게 null 처리 됨)
            launch {
                val c = java.util.Calendar.getInstance()
                val day = c.get(java.util.Calendar.DAY_OF_WEEK)
                val isWeekend = day == java.util.Calendar.SATURDAY || day == java.util.Calendar.SUNDAY
                _timeTable.value = stationRepository.getStationTimeTable(stationName, lineNumber, isWeekend)
            }
            launch {
                _transferInfo.value = stationRepository.getFastTransferInfo(stationName)
            }
            launch {
                _facilityInfo.value = stationRepository.getStationFacilityInfo(stationName)
            }
        }
    }
}