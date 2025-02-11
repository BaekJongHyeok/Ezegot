package com.jonghyeok.ezegot.viewModel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.repository.LocationRepository
import com.jonghyeok.ezegot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class MainViewModel(
    private val repository: MainRepository,
    private val locationRepository: LocationRepository
): ViewModel() {
    private val _favoriteStationList = MutableStateFlow<List<BasicStationInfo>>(emptyList())
    val favoriteStationList: StateFlow<List<BasicStationInfo>> = _favoriteStationList.asStateFlow()

    private val _realtimeArrivalInfo = MutableStateFlow<Map<String, List<RealtimeArrival>>>(emptyMap())
    val realtimeArrivalInfo: StateFlow<Map<String, List<RealtimeArrival>>> = _realtimeArrivalInfo.asStateFlow()

    private val _nearbyStationList = MutableStateFlow<List<BasicStationInfo>>(emptyList())
    val nearbyStationList: StateFlow<List<BasicStationInfo>> = _nearbyStationList.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    // 즐겨찾기 역 리스트
    fun updateFavoriteStation() {
        _favoriteStationList.value = repository.getFavoriteStations()
    }

    // 실시간 도착 정보 조회
    fun loadRealtimeArrival() {
        viewModelScope.launch {
            val updatedArrivalInfo = mutableMapOf<String, List<RealtimeArrival>>()

            _favoriteStationList.value.forEach { favoriteStation ->
                val arrivalInfo = repository.getRealtimeArrivalInfo(favoriteStation.stationName)
                updatedArrivalInfo[favoriteStation.stationName] = arrivalInfo
            }

            _realtimeArrivalInfo.value = updatedArrivalInfo
        }
    }

    // 근처 역 정보
    fun fetchNearbyStations(latitude: Double, longitude: Double) {
        // 저장된 역 위치 리스트 가져오기 (SharedPreference에서 가져온 역 위치 정보)
        val stationsLocationList = repository.getStationsLocationList()

        // 3km 이내의 역들 필터링 후 거리 기준으로 정렬
        val nearbyStations = stationsLocationList
            .map { station ->
                val stationLat = station.latitude
                val stationLon = station.longitude

                // 현재 좌표와 역의 좌표 간 거리 계산
                val distance = calculateDistance(latitude, longitude, stationLat, stationLon)

                // 각 역에 거리 정보 추가
                station to distance
            }
            .filter { (_, distance) -> distance <= 3 } // 3km 이내의 역만 필터링
            .sortedBy { (_, distance) -> distance } // 거리 기준으로 오름차순 정렬
            .map { (station, _) ->
                BasicStationInfo(
                    stationName = station.stationName,
                    lineNumber = station.lineName
                )
            }

        // 정렬된 근처 역들 업데이트
        _nearbyStationList.value = nearbyStations
    }


    // 위치 업데이트 요청
    fun updateCurrentLocation() {
        viewModelScope.launch {
            locationRepository.requestLocationUpdates()
                .collect { location ->
                    _currentLocation.value = location

                    // 위치가 업데이트될 때마다 근처 역 목록 갱신
                    location?.let {
                        fetchNearbyStations(it.latitude, it.longitude)
                    }
                }
        }
    }

    // 거리 계산 함수 (Haversine 공식)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // 지구 반지름 (단위: km)
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c // 두 지점 간의 거리 (단위: km)
    }
}