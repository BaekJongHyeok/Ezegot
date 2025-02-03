package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.dto.StationInfo

class SplashRepository(
    private val pm: SharedPreferenceManager
) {
    fun saveAllStationList(allStationList: List<StationInfo>) {
        pm.saveAllStationList(allStationList)
    }
}