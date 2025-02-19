package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.repository.SharedRepository
import kotlinx.coroutines.launch

class SplashViewModel(private val sharedRepository: SharedRepository) : BaseViewModel() {

    init {
        loadAllStationInfos()
    }

    private fun loadAllStationInfos() {
        viewModelScope.launch {
            val stationsInfo = loadAllStationsInfo()
            if (stationsInfo.isNotEmpty()) {
                sharedRepository.updateStations(stationsInfo)
            }
        }
    }
}