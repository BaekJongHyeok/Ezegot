package com.jonghyeok.ezegot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val stationPreferences: SharedPreferenceManager = SharedPreferenceManager(App.context)

    private val _stationArrivalInfo = MutableStateFlow<List<RealtimeArrival>>(emptyList())
    val stationArrivalInfo: StateFlow<List<RealtimeArrival>> = _stationArrivalInfo.asStateFlow()


    fun fetchStationArrivalInfo(stationName: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api2.getStationArrivalInfo(stationName)
                _stationArrivalInfo.value = response.arrivals
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getFavoriteStations(): List<BasicStationInfo> {
        return stationPreferences.getSavedStations().toList()
    }

    private val _stationNames = MutableStateFlow(
        listOf("수원", "가산디지털단지", "매탄권선", "수원시청")
    )
    val stationNames: StateFlow<List<String>> = _stationNames
}