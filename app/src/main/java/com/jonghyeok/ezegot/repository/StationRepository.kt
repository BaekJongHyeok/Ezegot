package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.api.SubwayApiService
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class StationRepository(
    private val apiService: SubwayApiService,
    private val pm: SharedPreferenceManager
) {
    suspend fun getRealtimeArrivalInfo(stationName: String): List<RealtimeArrival> {
        return try {
            val response = withContext(Dispatchers.IO) { apiService.getStationArrivalInfo(stationName) }
            response.arrivals
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun isStationSaved(stationInfo: BasicStationInfo): Boolean {
        return pm.isStationSaved(stationInfo)
    }

    fun removeStation(stationInfo: BasicStationInfo) {
        pm.removeStation(stationInfo)
    }

    fun saveStation(stationInfo: BasicStationInfo) {
        pm.saveStation(stationInfo)
    }
}