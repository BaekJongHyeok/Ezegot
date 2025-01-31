package com.jonghyeok.ezegot.api

import com.jonghyeok.ezegot.StationArrivalResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface SubwayApiService {
    @GET("6671597a506a6f6e3331796a705066/xml/SearchInfoBySubwayNameService/1/800")
    suspend fun getStations(): StationResponse

    @GET("api/subway/6671597a506a6f6e3331796a705066/xml/realtimeStationArrival/0/20/{stationName}")
    suspend fun getStationArrivalInfo(@Path("stationName") stationName: String): StationArrivalResponse
}