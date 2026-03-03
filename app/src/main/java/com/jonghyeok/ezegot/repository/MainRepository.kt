package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.api.SubwayApiService
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.dto.StationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 앱 전역에서 사용하는 주요 데이터 Repository.
 *
 * [StationInfo] 목록(전체 역)과 [StationInfoResponse] 위치 목록은
 * 앱 라이프사이클 동안 거의 변경되지 않는 정적 데이터이므로
 * @Singleton 스코프를 활용해 인메모리 캐시로 관리한다.
 *
 * 캐시 전략: Lazy-load + null-check
 *   - 첫 요청 시 API 호출 후 메모리에 보관
 *   - 이후 요청은 캐시에서 즉시 반환 (네트워크 0회)
 *   - 앱 재시작 시 캐시 무효화 (프로세스 종료 = 캐시 소멸)
 */
@Singleton
class MainRepository @Inject constructor(
    @Named("stationInfoApi")     private val stationInfoApi: SubwayApiService,
    @Named("realtimeArrivalApi") private val realtimeApi: SubwayApiService,
    @Named("stationLocationApi") private val locationApi: SubwayApiService
) {

    // ── In-memory cache ───────────────────────────────────────────
    // volatile: 멀티스레드 환경에서 가시성 보장
    @Volatile private var cachedStations: List<StationInfo>? = null
    @Volatile private var cachedLocations: List<StationInfoResponse>? = null

    // ── 전체 역 정보 (캐시) ────────────────────────────────────────
    /**
     * 전체 역 목록을 반환한다.
     * 두 번째 호출부터는 API를 호출하지 않고 캐시를 반환한다.
     *
     * 동시 호출 시 이중 초기화가 발생할 수 있지만, 결과는 동일하므로
     * 실무에서는 추가 동기화(중복 비용)보다 이 방식이 성능상 유리하다.
     */
    suspend fun getAllStations(): List<StationInfo> =
        cachedStations ?: fetchStationsFromApi().also { cachedStations = it }

    private suspend fun fetchStationsFromApi(): List<StationInfo> =
        runCatching {
            withContext(Dispatchers.IO) { stationInfoApi.getStations().stationList }
        }.getOrDefault(emptyList())

    // ── 역 위경도 목록 (캐시) ─────────────────────────────────────
    /**
     * 역 위치 목록을 반환한다.
     * 전체 역 목록과 마찬가지로 정적 데이터이므로 캐시한다.
     */
    suspend fun getStationsLocation(): List<StationInfoResponse> =
        cachedLocations ?: fetchLocationsFromApi().also { cachedLocations = it }

    private suspend fun fetchLocationsFromApi(): List<StationInfoResponse> =
        runCatching {
            withContext(Dispatchers.IO) {
                locationApi.getStationsLocation().body() ?: emptyList()
            }
        }.getOrDefault(emptyList())

    // ── 실시간 도착 정보 (캐시 없음 – 항상 최신 필요) ────────────────
    /**
     * 실시간 도착 정보는 매 새로고침마다 최신값이 필요하므로 캐시하지 않는다.
     */
    suspend fun getRealtimeArrival(stationName: String): List<RealtimeArrival> {
        val normalizedName = if (stationName == "서울역") "서울" else stationName
        return runCatching {
            withContext(Dispatchers.IO) { realtimeApi.getStationArrivalInfo(normalizedName).arrivals }
        }.getOrDefault(emptyList())
    }

    // ── 캐시 명시적 무효화 (예: pull-to-refresh) ─────────────────
    fun invalidateCache() {
        cachedStations = null
        cachedLocations = null
    }
}