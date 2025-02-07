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
        return pm.isFavoriteStation(stationInfo)
    }

    fun removeStation(stationInfo: BasicStationInfo) {
        pm.removeFavoriteStation(stationInfo)
    }

    fun addStation(stationInfo: BasicStationInfo) {
        pm.addFavoriteStation(stationInfo)
    }


    fun isNotification(stationInfo: BasicStationInfo): Boolean {
        return pm.isNotification(stationInfo)
    }

    fun removeNotification(stationInfo: BasicStationInfo) {
        pm.removeNotificationStation(stationInfo)
    }

    fun addNotification(stationInfo: BasicStationInfo) {
        pm.addNotificationStation(stationInfo)
    }
}