package com.jonghyeok.ezegot.viewModel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.repository.LocationRepository
import com.jonghyeok.ezegot.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

class MainViewModel(
    private val repository: MainRepository,
    private val locationRepository: LocationRepository
): BaseViewModel() {

//    private val _allStationList = MutableStateFlow(repository.getAllStationList())
//    val stationList: StateFlow<List<StationInfo>> = _allStationList.asStateFlow()

    private val _favoriteStationList = MutableStateFlow(repository.getFavoriteStations())
    val favoriteStationList: StateFlow<List<BasicStationInfo>> = _favoriteStationList.asStateFlow()

    private val _realtimeArrivalInfo = MutableStateFlow<Map<String, List<RealtimeArrival>>>(emptyMap())
    val realtimeArrivalInfo: StateFlow<Map<String, List<RealtimeArrival>>> = _realtimeArrivalInfo.asStateFlow()

    private val _nearbyStationList = MutableStateFlow<List<BasicStationInfo>>(emptyList())
    val nearbyStationList: StateFlow<List<BasicStationInfo>> = _nearbyStationList.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    init {
        updateFavoriteStation()
    }


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
    private fun fetchNearbyStations(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val stationsLocationList = repository.getStationsLocationList()
            val allStationList = allStationsInfoList.value

            // 1차 필터링: 맨해튼 거리(근사값)로 3.5km 이하만 남김
            val roughFilteredStations = stationsLocationList.filter { station ->
                fastDistance(latitude, longitude, station.latitude, station.longitude) < 3.5
            }

            // 2차 필터링: Haversine 적용 후 3km 이내 정렬
            val nearbyLocations = roughFilteredStations
                .mapNotNull { station ->
                    val distance = calculateDistance(latitude, longitude, station.latitude, station.longitude)
                    if (distance <= 3) station to distance else null
                }
                .sortedBy { it.second } // 거리 기준 정렬

            val groupedStations = nearbyLocations.flatMap { (location, _) ->
                allStationList.filter { it.stationName == location.stationName }
            }.map { station ->
                BasicStationInfo(
                    stationName = station.stationName,
                    lineNumber = station.lineNumber
                )
            }

            // UI 업데이트 (메인 스레드)
            withContext(Dispatchers.Main) {
                _nearbyStationList.value = groupedStations
            }
        }
    }

    // 맨해튼 거리 (빠른 근사 거리 계산)
    private fun fastDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return abs(lat1 - lat2) + abs(lon1 - lon2)
    }


    // 위치 업데이트 요청
    fun updateCurrentLocation() {
        viewModelScope.launch {
            locationRepository.requestLocationUpdates()
                .collect { location ->
                    if (_currentLocation.value != location) { // 중복 업데이트 방지
                        _currentLocation.value = location
                        fetchNearbyStations(location.latitude, location.longitude)
                    }
                }
        }
    }

    // 거리 계산 함수 (Haversine 공식)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // 지구 반지름 (단위: km)

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        return R * (2 * atan2(sqrt(a), sqrt(1 - a))) // 두 지점 간의 거리 (km)
    }
}