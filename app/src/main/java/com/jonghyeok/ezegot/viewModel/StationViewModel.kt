package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.alarm.SubwayAlarmManager
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.db.SubwayAlarmDao
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.FavoriteStation
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.repository.FavoriteRepository
import com.jonghyeok.ezegot.repository.LocationRepository
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
    private val locationRepository: LocationRepository,
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

        // barvlDt가 없으면 화면에 찍히는 추정 분을 초로 되돌려 쓴다.
        // 화면에서 선택지를 고를 때도 같은 값을 봐야 하므로 DTO로 옮겼다.
        val arrivalSeconds = arrival.secondsUntilArrival()

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
            favoriteRepository.isFavorite(stationName, line).collect { favorite ->
                _uiState.update { it.copy(isFavorite = favorite) }
            }
        }
    }

    fun loadArrivalInfo(stationName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val arrivals = stationRepository.getRealtimeArrivalInfo(stationName)
            _uiState.update { it.copy(arrivals = arrivals, isLoading = false) }
        }
    }

    /**
     * 이 역의 즐겨찾기를 토글한다.
     *
     * 상태는 Room Flow가 다시 흘려주므로 여기서 직접 갱신하지 않는다.
     */
    fun toggleFavorite() {
        val station = _uiState.value.stationInfo ?: return
        val favorite = FavoriteStation(station.stationName, station.lineNumber)
        viewModelScope.launch {
            if (_uiState.value.isFavorite) {
                favoriteRepository.removeFavorite(favorite)
            } else {
                favoriteRepository.addFavorite(favorite)
            }
        }
    }

    fun toggleNotification() {
        _uiState.update { it.copy(isNotification = !it.isNotification) }
    }

    /**
     * 역 위치를 불러온다.
     *
     * 역 위경도 목록에서 찾지 못했을 때만 "현재 위치"를 대신 채운다.
     * 그 좌표는 이전에 화면이 넘겨주던 것을 Repository 조회로 옮겼고,
     * 위치를 얻지 못하면 서울시청 좌표를 쓴다(기존 동작과 동일).
     */
    fun loadStationLocation(stationName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val locations = stationRepository.getStationsLocationList()
            var target = locations.find { it.stationName == stationName }

            if (target != null) {
                val address = stationRepository.getAddress(target.latitude, target.longitude)
                target = target.copy(address = address)
            }

            val result = target ?: run {
                // 역을 못 찾은 경우에만 현재 위치가 필요하다
                val current = locationRepository.getCurrentLocation()
                val lat = current?.latitude ?: FALLBACK_LATITUDE
                val lon = current?.longitude ?: FALLBACK_LONGITUDE
                StationInfoResponse(
                    stationId = "default",
                    stationName = "현재 위치",
                    lineName = "N/A",
                    longitude = lon,
                    latitude = lat,
                    address = stationRepository.getAddress(lat, lon)
                )
            }

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

    companion object {
        // 위치를 얻지 못했을 때 쓰는 대체 좌표 (서울시청)
        private const val FALLBACK_LATITUDE = 37.5665
        private const val FALLBACK_LONGITUDE = 126.9780
    }
}
