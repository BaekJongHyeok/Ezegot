package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.dto.StationInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedRepository {
    private val _allStationsInfoList = MutableStateFlow<List<StationInfo>>(emptyList())
    val allStationsInfoList: StateFlow<List<StationInfo>> = _allStationsInfoList

    // Method to update the station list
    fun updateStations(stations: List<StationInfo>) {
        _allStationsInfoList.value = stations
    }
}