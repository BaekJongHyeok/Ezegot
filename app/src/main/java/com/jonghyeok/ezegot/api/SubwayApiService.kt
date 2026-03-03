package com.jonghyeok.ezegot.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SubwayApiService {
    @GET("6671597a506a6f6e3331796a705066/xml/SearchInfoBySubwayNameService/1/800")
    suspend fun getStations(): StationResponse

    @GET("api/subway/6671597a506a6f6e3331796a705066/xml/realtimeStationArrival/0/20/{stationName}")
    suspend fun getStationArrivalInfo(@Path("stationName") stationName: String): StationArrivalResponse

    @GET("apig/apiman-gateway/tapi/TaimsKsccDvSubwayStationGeom/1.0?apikey=67c18d19-6b1e-49cd-affd-3a1e6c31db79")
    suspend fun getStationsLocation(): Response<List<StationInfoResponse>>

    // =========================================================================
    // 새로 추가될 고급 기능 (빠른 환승, 편의시설, 시간표) API 엔드포인트 틀
    // =========================================================================

    /** 
     * [서울 열린데이터 광장] 특정 역의 상/하행 평일/주말 시간표 (첫차/막차 조회용)
     * 예시 URL: http://openAPI.seoul.go.kr:8088/{API_KEY}/xml/SearchSTNTimeTableByFRCodeService/1/500/{전철역코드}/{요일코드}/{상하행코드}/
     */
    @GET("{apiKey}/xml/SearchSTNTimeTableByFRCodeService/1/500/{stationCode}/{weekCode}/{upDownCode}/")
    suspend fun getStationTimeTable(
        @Path("apiKey") apiKey: String,
        @Path("stationCode") stationCode: String,
        @Path("weekCode") weekCode: String,     // 1:평일, 2:토요일, 3:휴일/일요일
        @Path("upDownCode") upDownCode: String  // 1:상행/내선, 2:하행/외선
    ): TimeTableResponse

    /** 
     * [공공데이터포털] 출구 및 환승 정보 (빠른 환승 위치) 
     * BaseUrl: http://apis.data.go.kr/
     */
    @GET("api/transfer/info/{stationName}") // 실제 API 명세에 따라 수정 필요
    suspend fun getFastTransferInfo(@Path("stationName") stationName: String): TransferInfoResponse

    /** 
     * [공공데이터포털] 역 편의시설 정보 (엘리베이터, 화장실 등)
     * BaseUrl: http://apis.data.go.kr/
     */
    @GET("api/facility/info/{stationName}") // 실제 API 명세에 따라 수정 필요
    suspend fun getStationFacilityInfo(@Path("stationName") stationName: String): FacilityInfoResponse

    /** 
     * [공공데이터포털] TAGO 지하철 정보 - 역 목록 조회 (역명으로 ID 찾기)
     */
    @GET("1613000/SubwayInfoService/getKwrdFndSubwaySttnList")
    suspend fun getTagoStationList(
        @retrofit2.http.Query("serviceKey", encoded = true) serviceKey: String,
        @retrofit2.http.Query("subwayStationName") stationName: String,
        @retrofit2.http.Query("_type") type: String = "json",
        @retrofit2.http.Query("numOfRows") numOfRows: Int = 100
    ): Response<com.google.gson.JsonElement>

    /** 
     * [공공데이터포털] TAGO 지하철 정보 - 지하철역별 시간표 목록 조회
     */
    @GET("1613000/SubwayInfoService/getSubwaySttnAcctoSchdulList")
    suspend fun getTagoTimeTable(
        @retrofit2.http.Query("serviceKey", encoded = true) serviceKey: String,
        @retrofit2.http.Query("subwayStationId") stationId: String,
        @retrofit2.http.Query("dailyTypeCode") dailyTypeCode: String, // 01:평일, 02:토요일, 03:일/공휴일
        @retrofit2.http.Query("upDownTypeCode") upDownTypeCode: String, // U:상행, D:하행
        @retrofit2.http.Query("_type") type: String = "json",
        @retrofit2.http.Query("numOfRows") numOfRows: Int = 500
    ): Response<com.google.gson.JsonElement>
}