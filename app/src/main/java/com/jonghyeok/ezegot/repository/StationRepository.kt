package com.jonghyeok.ezegot.repository

import android.location.Geocoder
import com.jonghyeok.ezegot.MyApplication
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.api.SubwayApiService
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 역 상세 화면 Repository.
 *
 * 위치 목록([getStationsLocationList])은 [MainRepository]의 캐시를 거쳐서 반환되므로
 * 역 상세 진입마다 API를 새로 호출하지 않는다.
 */
@Singleton
class StationRepository @Inject constructor(
    @Named("realtimeArrivalApi") private val realtimeApi: SubwayApiService,
    @Named("extendedApi") private val extendedApi: SubwayApiService,
    @Named("stationInfoApi") private val stationInfoApi: SubwayApiService,
    private val mainRepository: MainRepository  // 위치 목록 캐시 공유
) {
    private val arrivalCache = mutableMapOf<String, Pair<Long, List<RealtimeArrival>>>()
    private val CACHE_DURATION_MS = 10_000L // 10초 캐시
    
    // 에러 발생으로 인한 재시도 락 (하루 호출량 초과 방지)
    private var isApiLocked = false
    private var retryCount = 0
    private val MAX_RETRIES = 3

    /** 
     * 실시간 도착 정보 – 10초 메모리 캐시 및 재시도 락 적용
     */
    suspend fun getRealtimeArrivalInfo(stationName: String): List<RealtimeArrival> {
        val normalizedName = normalizeStationNameForRealtime(stationName)
        
        if (isApiLocked) {
            // 이미 락이 걸렸다면 불필요한 호출을 막고 빈 리스트(또는 캐시) 반환
            return arrivalCache[normalizedName]?.second ?: emptyList()
        }

        val now = System.currentTimeMillis()
        val cached = arrivalCache[normalizedName]
        if (cached != null && (now - cached.first) < CACHE_DURATION_MS) {
            return cached.second
        }

        return runCatching {
            withContext(Dispatchers.IO) { 
                val response = realtimeApi.getStationArrivalInfo(normalizedName)
                
                // 에러 코드 337 (일일 호출량 초과) 등 서버 에러 시 예외 발생 유도
                val code = response.arrivalResult.status
                val msg = response.arrivalResult.message
                if (code == "ERROR-337" || msg.contains("데이터요청은 일일 호출건수 최대 1000건을 넘을 수 없습니다")) {
                    throw IllegalStateException("API Quota Exceeded or Server Error")
                }
                
                val result = response.arrivals ?: emptyList()
                
                // 성공 시 락 해제 및 캐시 저장
                retryCount = 0
                arrivalCache[normalizedName] = Pair(now, result)
                result
            }
        }.onFailure {
            retryCount++
            if (retryCount >= MAX_RETRIES) {
                isApiLocked = true
            }
        }.getOrDefault(arrivalCache[normalizedName]?.second ?: emptyList())
    }

    /**
     * 서울 실시간 도착 API는 "서울역" 대신 "서울"을 써야만 데이터를 주는 특이 케이스가 있음.
     */
    private fun normalizeStationNameForRealtime(stationName: String): String {
        return if (stationName == "서울역") "서울" else stationName
    }

    /**
     * 역 위치 목록 – MainRepository 캐시 위임.
     *
     * Before: 역 상세 진입마다 API 호출
     * After : MainRepository @Singleton 캐시 → 최초 1회 API 후 즉시 반환
     */
    suspend fun getStationsLocationList(): List<StationInfoResponse> =
        mainRepository.getStationsLocation()

    /** 위경도 → 주소 역지오코딩 */
    suspend fun getAddress(lat: Double, lon: Double): String =
        withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(MyApplication.context, Locale.getDefault())
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lon, 1)
                    ?.firstOrNull()
                    ?.getAddressLine(0)
                    ?: "주소를 찾을 수 없습니다."
            }.getOrDefault("주소를 가져올 수 없음")
        }

    suspend fun isFavorite(station: BasicStationInfo, dao: com.jonghyeok.ezegot.db.FavoriteStationDao): Boolean =
        dao.exists(station.stationName, station.lineNumber)

    // =========================================================================
    // 새로 추가될 기능 연동: 빠른 환승, 역 편의시설, 시간표
    // =========================================================================
    /** 첫차 / 막차 조회를 위한 시간표 가져오기 (상/하행 모두 반환) */
    suspend fun getStationTimeTable(stationName: String, lineNumber: String, isWeekend: Boolean): Pair<com.jonghyeok.ezegot.api.TimeTableResponse?, com.jonghyeok.ezegot.api.TimeTableResponse?> =
        withContext(Dispatchers.IO) {
            val stationsResponse = runCatching { stationInfoApi.getStations() }.getOrNull()
            val cleanName = stationName.replace("역", "")
            val numValue = lineNumber.filter { it.isDigit() }.let { if (it.isEmpty()) lineNumber else it }

            var frCode = stationsResponse?.stationList?.find {
                it.stationName.contains(cleanName) && it.lineNumber.contains(numValue)
            }?.frCode

            if (frCode.isNullOrEmpty()) {
                frCode = stationsResponse?.stationList?.find { it.stationName.contains(cleanName) }?.frCode
            }

            // ── 서울 열린데이터 API 시도 (FR_CODE가 있을 때만) ────────────────
            var up: com.jonghyeok.ezegot.api.TimeTableResponse? = null
            var down: com.jonghyeok.ezegot.api.TimeTableResponse? = null

            if (!frCode.isNullOrEmpty()) {
                val apiKey = "6b684557416a6f6e3532634f584472"
                val weekCode = if (isWeekend) "2" else "1"
                up = runCatching { stationInfoApi.getStationTimeTable(apiKey, frCode, weekCode, "1") }.getOrNull()
                down = runCatching { stationInfoApi.getStationTimeTable(apiKey, frCode, weekCode, "2") }.getOrNull()

                // DEBUG
                up?.schedules?.take(3)?.forEach { s ->
                    android.util.Log.d("TimetableDebug", "[Seoul API] leftTime=${s.leftTime}, dest=${s.destination}, express='${s.express}'")
                }
            }

            val upEmpty = up == null || up.schedules.isEmpty()
            val downEmpty = down == null || down.schedules.isEmpty()

            // ── TAGO API Fallback (코레일/전국 역, 또는 서울 API 데이터 없을 때) ─
            if (upEmpty && downEmpty) {
                val tagoKey = "n66XMJj%2Fdykub00YEwFWgMZ%2BMTjohpj5LYwNMRHLnKK9hSG%2FoQGAEgh64d8FyBlOWKPsRWE63k2wkmqaCQR1Zg%3D%3D"

                val tagoListRes = runCatching { extendedApi.getTagoStationList(tagoKey, cleanName) }.getOrNull()
                var nodeId: String? = null

                tagoListRes?.body()?.let { json ->
                    try {
                        val items = json.asJsonObject.getAsJsonObject("response").getAsJsonObject("body").getAsJsonObject("items")
                        if (items.has("item")) {
                            val itemElement = items.get("item")
                            val itemList = if (itemElement.isJsonArray) itemElement.asJsonArray else com.google.gson.JsonArray().apply { add(itemElement) }

                            for (item in itemList) {
                                val obj = item.asJsonObject
                                val routeName = obj.get("subwayRouteName")?.asString ?: ""
                                if (routeName.contains(numValue) || lineNumber.contains(routeName.replace("선", ""))) {
                                    nodeId = obj.get("subwayStationId")?.asString
                                    break
                                }
                            }
                            if (nodeId == null && itemList.size() > 0) {
                                nodeId = itemList.get(0).asJsonObject.get("subwayStationId")?.asString
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (nodeId != null) {
                    val c = java.util.Calendar.getInstance()
                    val tagoDayCode = when (c.get(java.util.Calendar.DAY_OF_WEEK)) {
                        java.util.Calendar.SATURDAY -> "02"
                        java.util.Calendar.SUNDAY   -> "03"
                        else                         -> "01"
                    }
                    val tagoUp   = fetchTagoTimeTable(nodeId!!, tagoDayCode, "U", tagoKey, extendedApi)
                    val tagoDown = fetchTagoTimeTable(nodeId!!, tagoDayCode, "D", tagoKey, extendedApi)
                    return@withContext Pair(tagoUp, tagoDown)
                }
            }
            Pair(up, down)
        }


    /** 빠른 환승 위치 정보 가져오기 */
    suspend fun getFastTransferInfo(stationName: String): com.jonghyeok.ezegot.api.TransferInfoResponse? =
        withContext(Dispatchers.IO) {
            runCatching {
                extendedApi.getFastTransferInfo(stationName)
            }.getOrNull()
        }

    /** 역 편의시설 (엘리베이터 유무 등) 정보 가져오기 */
    suspend fun getStationFacilityInfo(stationName: String): com.jonghyeok.ezegot.api.FacilityInfoResponse? =
        withContext(Dispatchers.IO) {
            runCatching {
                extendedApi.getStationFacilityInfo(stationName)
            }.getOrNull()
        }

    // TAGO API JSON 파싱 헬퍼 함수
    private suspend fun fetchTagoTimeTable(
        nodeId: String, 
        dayCode: String, 
        upDownCode: String, 
        key: String,
        api: SubwayApiService
    ): com.jonghyeok.ezegot.api.TimeTableResponse? {
        val res = runCatching { api.getTagoTimeTable(key, nodeId, dayCode, upDownCode) }.getOrNull() ?: return null
        return try {
            val bodyElement = res.body()?.asJsonObject?.getAsJsonObject("response")?.getAsJsonObject("body")
            val items = bodyElement?.getAsJsonObject("items")
            
            val schedules = mutableListOf<com.jonghyeok.ezegot.api.TimeTableSchedule>()
            if (items != null && items.has("item")) {
                val itemElement = items.get("item")
                val itemList = if (itemElement.isJsonArray) itemElement.asJsonArray else com.google.gson.JsonArray().apply { add(itemElement) }
                for (i in itemList) {
                    val obj = i.asJsonObject
                    val depTime = obj.get("depTime")?.asString ?: ""
                    // HHmmss -> HH:mm:ss 포맷팅
                    val formattedTime = if (depTime.length == 6) "${depTime.substring(0,2)}:${depTime.substring(2,4)}:${depTime.substring(4,6)}" else depTime
                    val dest = obj.get("endSubwayStationNm")?.asString ?: ""
                    
                    // 급행 판단 로직 (TAGO API에서 G 등 특정 값으로 내려줄 수 있음, 없다면 "" 할당)
                    val expressVal = obj.get("exprnYn")?.asString ?: obj.get("expressYn")?.asString ?: ""
                    android.util.Log.d("TimetableDebug", "[TAGO API] depTime=$depTime, exprnYn raw='$expressVal'")
                    val isExpress = if (expressVal.equals("Y", ignoreCase = true)) "Y" else ""

                    schedules.add(com.jonghyeok.ezegot.api.TimeTableSchedule(leftTime = formattedTime, destination = dest, express = isExpress))
                }
            }
            if (schedules.isNotEmpty()) {
                com.jonghyeok.ezegot.api.TimeTableResponse(schedules)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}