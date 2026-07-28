package com.jonghyeok.ezegot.api

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 지하철 관련 공공 API 인터페이스.
 *
 * 같은 인터페이스를 baseUrl이 다른 여러 Retrofit 인스턴스에 붙여 쓰고,
 * `@Named` 한정자로 구분한다 (`di/AppModule.kt` 참고).
 *
 * API 키는 소스에 두지 않고 호출부에서 인자로 넘긴다.
 * 실제 값은 `BuildConfig`(→ `local.properties`)에서 온다.
 */
interface SubwayApiService {

    /** [서울 열린데이터광장] 전체 역 목록 */
    @GET("{apiKey}/xml/SearchInfoBySubwayNameService/1/800")
    suspend fun getStations(
        @Path("apiKey") apiKey: String
    ): StationResponse

    /** [서울 열린데이터광장] 특정 역의 실시간 도착 정보 */
    @GET("api/subway/{apiKey}/xml/realtimeStationArrival/0/20/{stationName}")
    suspend fun getStationArrivalInfo(
        @Path("apiKey") apiKey: String,
        @Path("stationName") stationName: String
    ): StationArrivalResponse

    /** [서울 교통 데이터(t-data)] 전체 역 위경도 */
    @GET("apig/apiman-gateway/tapi/TaimsKsccDvSubwayStationGeom/1.0")
    suspend fun getStationsLocation(
        @Query("apikey") apiKey: String
    ): Response<List<StationInfoResponse>>

    /**
     * [서울 열린데이터광장] 특정 역의 상/하행 평일/주말 시간표 (첫차·막차 조회용)
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

    /** [공공데이터포털] TAGO 지하철 정보 – 역 목록 조회 (역명으로 ID 찾기) */
    @GET("1613000/SubwayInfoService/getKwrdFndSubwaySttnList")
    suspend fun getTagoStationList(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("subwayStationName") stationName: String,
        @Query("_type") type: String = "json",
        @Query("numOfRows") numOfRows: Int = 100
    ): Response<JsonElement>

    /** [공공데이터포털] TAGO 지하철 정보 – 지하철역별 시간표 목록 조회 */
    @GET("1613000/SubwayInfoService/getSubwaySttnAcctoSchdulList")
    suspend fun getTagoTimeTable(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("subwayStationId") stationId: String,
        @Query("dailyTypeCode") dailyTypeCode: String,   // 01:평일, 02:토요일, 03:일/공휴일
        @Query("upDownTypeCode") upDownTypeCode: String, // U:상행, D:하행
        @Query("_type") type: String = "json",
        @Query("numOfRows") numOfRows: Int = 500
    ): Response<JsonElement>
}
