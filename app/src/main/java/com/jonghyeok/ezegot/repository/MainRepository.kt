package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.api.SubwayApiService
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepository(
    private val apiService: SubwayApiService,
    private val pm : SharedPreferenceManager
) {

    // 즐겨찾기 저장된 역 정보
    fun getFavoriteStations(): List<BasicStationInfo> {
        return pm.getFavoriteStations()
    }

    // 실시간 도착 정보 불러 오기
    suspend fun getRealtimeArrivalInfo(stationName: String): List<RealtimeArrival> {
        return try {
            val response = withContext(Dispatchers.IO) { apiService.getStationArrivalInfo(stationName) }
            response.arrivals
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getStationsLocationList(): List<StationInfoResponse> {
        return pm.getStationsLocationList()
    }
}