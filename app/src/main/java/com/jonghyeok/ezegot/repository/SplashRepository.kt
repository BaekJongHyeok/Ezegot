package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.StationInfo

class SplashRepository(
    private val pm: SharedPreferenceManager
) {
    fun saveAllStationList(allStationList: List<StationInfo>) {
        pm.saveAllStationList(allStationList)
    }

    fun saveAllStationsLocationList(stationList: List<StationInfoResponse>) {
        pm.saveAllStationsLocation(stationList)
    }
}