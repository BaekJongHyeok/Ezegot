package com.jonghyeok.ezegot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StationViewModel: ViewModel() {
    private val stationPreferences: SharedPreferenceManager = SharedPreferenceManager(App.context)

    private val _stationArrivalInfo = MutableStateFlow<List<Arrival>>(emptyList())
    val stationArrivalInfo: StateFlow<List<Arrival>> = _stationArrivalInfo.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

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

    fun checkIfStationIsSaved(stationName: String) {
        _isSaved.value = stationPreferences.isStationSaved(stationName)
    }

    fun toggleStationFavorite(stationName: String) {
        if (_isSaved.value) {
            stationPreferences.removeStation(stationName)
        } else {
            stationPreferences.saveStation(stationName)
        }
        _isSaved.value = !_isSaved.value
    }
}