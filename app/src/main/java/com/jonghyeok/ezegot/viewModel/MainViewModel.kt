package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.jonghyeok.ezegot.App
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository): ViewModel() {
    private val _favoriteStationList = MutableStateFlow<List<BasicStationInfo>>(emptyList())
    val favoriteStationList: StateFlow<List<BasicStationInfo>> = _favoriteStationList.asStateFlow()

    private val _realtimeArrivalInfo = MutableStateFlow<Map<String, List<RealtimeArrival>>>(emptyMap())
    val realtimeArrivalInfo: StateFlow<Map<String, List<RealtimeArrival>>> = _realtimeArrivalInfo.asStateFlow()

    private val _nearbyStationList = MutableStateFlow<List<BasicStationInfo>>(emptyList())
    val nearbyStationList: StateFlow<List<BasicStationInfo>> = _nearbyStationList.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(App.context)

    // 즐겨찾기 역 리스트
    fun getFavoriteStationList() {
        _favoriteStationList.value = repository.getFavoriteStations()
    }

    // 실시간 도착 정보 조회
    fun fetchRealtimeArrival() {
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
        val nearbyStations = repository.getNearbyStations(latitude, longitude)

        _nearbyStationList.value = nearbyStations
    }
}