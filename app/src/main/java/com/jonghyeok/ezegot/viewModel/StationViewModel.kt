package com.jonghyeok.ezegot.viewModel

import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.jonghyeok.ezegot.MyApplication.Companion.context
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StationViewModel(private val repository: StationRepository) : ViewModel() {

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


    // 역 기초 정보 설정
    fun loadStationInfo(stationName: String, line: String) {
        val stationInfo = BasicStationInfo(stationName, line)
        _stationInfo.value = stationInfo
        _isFavorite.value = repository.isStationSaved(stationInfo)
        _isNotification.value = repository.isNotification(stationInfo)
    }

    // 실시간 도착 정보 조회
    fun loadArrivalInfo(stationName: String) {
        viewModelScope.launch {
            _arrivalInfo.value = repository.getRealtimeArrivalInfo(stationName)
        }
    }

    // 즐겨찾기 추가/삭제 토글
    fun toggleFavorite() {
        val station = _stationInfo.value ?: return
        if (_isFavorite.value) {
            repository.removeStation(station)
        } else {
            repository.addStation(station)
        }
        _isFavorite.value = !_isFavorite.value
    }

    // 알람 추가/ 삭제 토글
    fun toggleNotification() {
        val station = _stationInfo.value ?: return
        if (_isNotification.value) {
            repository.removeNotification(station)
        } else {
            repository.addNotification(station)
        }
        _isNotification.value = !_isNotification.value

    }

    // 역 위치 가져오기
    fun loadStationLocation(stationName: String, defaultLocation: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            val stationsLocationList = repository.getStationsLocationList()
            var targetStation = stationsLocationList.find { it.stationName == stationName }

            // 데이터가 있으면 해당 station의 주소를 업데이트
            if (targetStation != null) {
                val address = getAddressFromCoordinates(targetStation.latitude, targetStation.longitude)
                targetStation = targetStation.copy(address = address)
            }

            // 데이터가 없으면 기본 위치 사용
            val station = targetStation ?: StationInfoResponse(
                stationId = "default",
                stationName = "현재 위치",
                lineName = "N/A",
                longitude = defaultLocation.longitude,
                latitude = defaultLocation.latitude,
                address = getAddressFromCoordinates(defaultLocation.latitude, defaultLocation.longitude)
            )

            _stationLocation.value = station
        }
    }

    // 위경도로 주소 가져오기
    private fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context)
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

        return if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            address.getAddressLine(0) // 전체 주소
        } else {
            "주소를 찾을 수 없습니다."
        }
    }

}