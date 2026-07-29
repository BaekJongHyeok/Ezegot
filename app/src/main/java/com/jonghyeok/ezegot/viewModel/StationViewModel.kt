package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.jonghyeok.ezegot.alarm.SubwayAlarmManager
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.db.SubwayAlarmDao
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.repository.FavoriteRepository
import com.jonghyeok.ezegot.repository.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StationViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val favoriteRepository: FavoriteRepository,
    private val alarmManager: SubwayAlarmManager,
    private val alarmDao: SubwayAlarmDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationUiState())
    val uiState: StateFlow<StationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            alarmDao.getActiveAlarms().collect { alarms ->
                _uiState.update { it.copy(activeAlarms = alarms) }
            }
        }
    }

    fun setAlarm(arrival: RealtimeArrival, thresholdMinutes: Int = 3) {
        val station = _uiState.value.stationInfo ?: return

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

    fun loadStationInfo(stationName: String, line: String) {
        val info = BasicStationInfo(stationName, line)
        _uiState.update { it.copy(stationInfo = info) }
        viewModelScope.launch {
            val favorite = favoriteRepository.isFavorite(info)
            _uiState.update { it.copy(isFavorite = favorite) }
        }
    }

    fun loadArrivalInfo(stationName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val arrivals = stationRepository.getRealtimeArrivalInfo(stationName)
            _uiState.update { it.copy(arrivals = arrivals, isLoading = false) }
        }
    }

    fun toggleFavorite() {
        val station = _uiState.value.stationInfo ?: return
        viewModelScope.launch {
            val wasFavorite = _uiState.value.isFavorite
            if (wasFavorite) {
                favoriteRepository.removeFavorite(station)
            } else {
                favoriteRepository.addFavorite(station)
            }
            _uiState.update { it.copy(isFavorite = !wasFavorite) }
        }
    }

    fun toggleNotification() {
        _uiState.update { it.copy(isNotification = !it.isNotification) }
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

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(stationLocation = result) }
            }
        }
    }

    /**
     * 첫차·막차 시간표를 불러온다.
     *
     * 상·하행이 모두 null로 돌아오면 서울 API와 TAGO 폴백이 전부 실패한 경우다.
     * 이때는 로딩 placeholder에 머무르지 않도록 에러 메시지를 채운다.
     * 응답은 왔지만 데이터가 비어 있는 경우는 실패가 아니며, 화면이 별도로 안내한다.
     */
    fun loadAdvancedStationInfo(stationName: String, lineNumber: String) {
        viewModelScope.launch {
            val c = Calendar.getInstance()
            val day = c.get(Calendar.DAY_OF_WEEK)
            val isWeekend = day == Calendar.SATURDAY || day == Calendar.SUNDAY

            val timetable = stationRepository.getStationTimeTable(stationName, lineNumber, isWeekend)
            val failed = timetable.first == null && timetable.second == null

            _uiState.update {
                it.copy(
                    timetable = timetable,
                    errorMessage = if (failed) "시간표를 불러오지 못했습니다" else null
                )
            }
        }
    }
}
