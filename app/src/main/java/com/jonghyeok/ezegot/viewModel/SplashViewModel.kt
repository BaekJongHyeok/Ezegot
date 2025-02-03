package com.jonghyeok.ezegot.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.api.RetrofitInstance
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.repository.SplashRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: SplashRepository) : ViewModel() {
    private val _loadingState = MutableStateFlow(true)
    val loadingState: StateFlow<Boolean> get() = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> get() = _errorState

    private val _stationList = MutableStateFlow<List<StationInfo>>(emptyList())
    val stationList: StateFlow<List<StationInfo>> = _stationList

    fun fetchStations() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getStations()
                if (response.stationList.isNotEmpty()) {
                    _stationList.value = response.stationList
                    repository.saveAllStationList(response.stationList)
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
}
