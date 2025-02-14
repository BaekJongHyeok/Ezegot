package com.jonghyeok.ezegot.viewModel

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.App.Companion.context
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.repository.SplashRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class BaseViewModel<T : Any>(
    private val repository: T
) : ViewModel() {

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

    init {
        loadAllStationsInfo() // 전체 역 정보
        loadAllStationsLocation() // 전체 역 위치
    }

    private fun loadAllStationsInfo() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getStations()
                if (response.stationList.isNotEmpty()) {

                    _allStationsInfoList.value = response.stationList
                    val splashRepository: SplashRepository = repository as SplashRepository
                    splashRepository.saveAllStationList(response.stationList)

                } else {
                    _errorState.value = "No data available"
                }
            } catch (e: Exception) {
                _errorState.value = "Failed to fetch data"
                Log.e("SplashViewModel", "Error fetching stations: ${e.message}")
            } finally {
                _loadingState.value = false
            }
        }
    }

    private fun loadAllStationsLocation() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api3.getStationsLocation()

                if (response.isSuccessful) {
                    response.body()?.let { stationList ->
                        // 각 역의 위경도를 사용하여 주소를 가져옵니다.
                        val updatedStations = stationList.map { station ->
                            val address = getAddressFromCoordinates(station.latitude, station.longitude)
                            station.copy(address = address) // 주소를 추가한 새로운 StationInfo 반환
                        }

                        _allStationsLocationList.value = updatedStations
                        val splashRepository: SplashRepository = repository as SplashRepository
                        splashRepository.saveAllStationsLocationList(updatedStations) // 수정된 리스트 저장

                    } ?: run {
                        _errorState.value = "No data received"
                    }
                } else {
                    _errorState.value = "Failed to fetch station locations"
                }
            } catch (e: Exception) {
                _errorState.value = "Error: ${e.message}"
                Log.e("SplashViewModel", "Error fetching stations: ${e.message}")
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 위경도로 주소 가져오기
    private fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context)
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

        return if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            address.getAddressLine(0) // 전체 주소 반환
        } else {
            "주소를 찾을 수 없습니다."
        }
    }
}
