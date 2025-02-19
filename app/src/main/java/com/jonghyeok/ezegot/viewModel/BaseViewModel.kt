package com.jonghyeok.ezegot.viewModel

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.MyApplication.Companion.context
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.StationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

open class BaseViewModel : ViewModel() {

    private val _allStationsInfoList = MutableStateFlow<List<StationInfo>>(emptyList())
    val allStationsInfoList: StateFlow<List<StationInfo>> = _allStationsInfoList

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _isNotification = MutableStateFlow(false)
    val isNotification: StateFlow<Boolean> = _isNotification

    private val _allStationsLocationList = MutableStateFlow<List<StationInfoResponse>>(emptyList())
    val allStationsLocationList: StateFlow<List<StationInfoResponse>> = _allStationsLocationList


    private val _stationLocation = MutableStateFlow<StationInfoResponse?>(null)
    val stationLocation: StateFlow<StationInfoResponse?> = _stationLocation

    private val _loadingState = MutableStateFlow(true)
    val loadingState: StateFlow<Boolean> get() = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> get() = _errorState


    fun loadAllData() {
        viewModelScope.launch {
            val stationsInfoDeferred = async { loadAllStationsInfo() }
            val stationsLocationDeferred = async { loadAllStationsLocation() }

            val stationsInfo = stationsInfoDeferred.await()
            val stationsLocation = stationsLocationDeferred.await()

            // 모든 데이터가 로드된 이후 실행
            if (stationsInfo.isNotEmpty() && stationsLocation.isNotEmpty()) {
                _loadingState.value = false
                processStationAddresses()
            }
        }
    }


    suspend fun loadAllStationsInfo(): List<StationInfo> {
        return try {
            val response = RetrofitInstance.api.getStations()
            if (response.stationList.isNotEmpty()) {
                _allStationsInfoList.value = response.stationList
                response.stationList
            } else {
                _errorState.value = "No data available"
                emptyList()
            }
        } catch (e: Exception) {
            _errorState.value = "Failed to fetch data"
            Log.e("SplashViewModel", "Error fetching stations: ${e.message}")
            emptyList()
        }
    }


    private suspend fun loadAllStationsLocation(): List<StationInfoResponse> {
        return try {
            val response = RetrofitInstance.api3.getStationsLocation()
            if (response.isSuccessful) {
                response.body()?.let { stationList ->
                    _allStationsLocationList.value = stationList
                    stationList // 데이터를 반환
                } ?: run {
                    _errorState.value = "No data received"
                    emptyList()
                }
            } else {
                _errorState.value = "Failed to fetch station locations"
                emptyList()
            }
        } catch (e: Exception) {
            _errorState.value = "Error: ${e.message}"
            Log.e("SplashViewModel", "Error fetching station locations: ${e.message}")
            emptyList()
        }
    }

    // 위경도로 주소 가져오기
    private suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.getAddressLine(0) ?: "주소를 찾을 수 없습니다."
            } catch (e: IOException) {
                Log.e("SplashViewModel", "Geocoder Error: ${e.message}")
                "주소를 가져올 수 없음"
            }
        }
    }

    private suspend fun processStationAddresses() {
        val updatedStations = _allStationsLocationList.value.map { station ->
            val address = getAddressFromCoordinates(station.latitude, station.longitude)
            station.copy(address = address)
        }
        _allStationsLocationList.value = updatedStations
    }
}
