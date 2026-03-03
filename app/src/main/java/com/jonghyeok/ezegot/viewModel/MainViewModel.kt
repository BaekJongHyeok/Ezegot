package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.dto.NearbyStation
import com.jonghyeok.ezegot.repository.FavoriteRepository
import com.jonghyeok.ezegot.repository.LocationRepository
import com.jonghyeok.ezegot.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val favoriteRepository: FavoriteRepository,
    private val locationRepository: LocationRepository
) : BaseViewModel() {

    // ── 즐겨찾기 ─────────────────────────────────────────────────
    val favoriteStationList: StateFlow<List<BasicStationInfo>> = favoriteRepository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── 실시간 도착 정보 (stationName → List) ─────────────────────
    private val _realtimeArrivalInfo = MutableStateFlow<Map<String, List<RealtimeArrival>>>(emptyMap())
    val realtimeArrivalInfo: StateFlow<Map<String, List<RealtimeArrival>>> = _realtimeArrivalInfo.asStateFlow()

    // ── 개별 카드 로딩 상태 (stationName → Boolean) ───────────────
    private val _loadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val loadingStates: StateFlow<Map<String, Boolean>> = _loadingStates.asStateFlow()

    // ── 근처 역 ──────────────────────────────────────────────────
    private val _nearbyStationList = MutableStateFlow<List<NearbyStation>>(emptyList())
    val nearbyStationList: StateFlow<List<NearbyStation>> = _nearbyStationList.asStateFlow()

    private var stationNameMap: Map<String, List<StationInfo>> = emptyMap()
    private var locationsCache: List<StationInfoResponse> = emptyList()

    // 새로고침 시 이전 Job 취소용
    private var arrivalJob: Job? = null

    init {
        loadInitialData()
        observeFavoritesForAutoRefresh()
    }

    // ── 초기 데이터 로드 ─────────────────────────────────────────
    private fun loadInitialData() {
        viewModelScope.launch {
            val stationsDeferred  = launch { mainRepository.getAllStations().let { s ->
                setAllStations(s)
                stationNameMap = s.groupBy { it.stationName }
            }}
            val locationsDeferred = launch { mainRepository.getStationsLocation().also { locationsCache = it } }
            stationsDeferred.join()
            locationsDeferred.join()
        }
    }

    // ── 즐겨찾기 변경 시 자동 도착정보 갱신 ─────────────────────
    /**
     * 즐겨찾기 목록이 변경될 때(추가/삭제) 자동으로 도착 정보를 갱신한다.
     * 화면이 열릴 때도 즐겨찾기 로드와 동시에 API를 시작하므로
     * 별도 loadRealtimeArrival() 호출 없이도 빠르게 표시된다.
     */
    private fun observeFavoritesForAutoRefresh() {
        viewModelScope.launch {
            favoriteStationList
                .map { it.map { s -> s.stationName } }
                .distinctUntilChanged()
                .collect { names ->
                    streamRealtimeArrival(names)
                }
        }
    }

    // ── 스트리밍 방식 도착 정보 로드 ─────────────────────────────
    /**
     * 각 역을 독립 launch로 실행 → 응답 오는 순서대로 카드 즉시 업데이트.
     *
     * Before (awaitAll):
     *   역A(1s) + 역B(3s) + 역C(2s) → 3초 후 한꺼번에 표시
     *
     * After (streaming):
     *   역A(1s) → 1초 후 카드A 표시
     *   역C(2s) → 2초 후 카드C 표시
     *   역B(3s) → 3초 후 카드B 표시  (체감 지연: 1초)
     */
    fun loadRealtimeArrival() {
        val favorites = favoriteStationList.value
        if (favorites.isEmpty()) return
        streamRealtimeArrival(favorites.map { it.stationName })
    }

    private fun streamRealtimeArrival(stationNames: List<String>) {
        // 이전 갱신 취소 후 재시작
        arrivalJob?.cancel()

        if (stationNames.isEmpty()) {
            _realtimeArrivalInfo.value = emptyMap()
            _loadingStates.value = emptyMap()
            return
        }

        // 모든 역을 로딩 중으로 표시
        _loadingStates.value = stationNames.associateWith { true }

        arrivalJob = viewModelScope.launch {
            stationNames.forEach { name ->
                launch {   // 각 역이 독립 코루틴 → 병렬 + 순서 무관 즉시 반영
                    val arrivals = mainRepository.getRealtimeArrival(name)

                    // 응답이 오는 즉시 해당 카드만 업데이트 (전체 대기 없음)
                    _realtimeArrivalInfo.update { current -> current + (name to arrivals) }
                    _loadingStates.update   { current -> current + (name to false) }
                }
            }
        }
    }

    // ── GPS – Two-phase 패턴 ──────────────────────────────────────
    fun updateCurrentLocation() {
        viewModelScope.launch {
            // Phase 1: 캐시된 마지막 위치로 즉시 처리
            locationRepository.getLastKnownLocation()?.let { lastLoc ->
                fetchNearbyStations(lastLoc.latitude, lastLoc.longitude)
            }
            // Phase 2: 지속 GPS 갱신
            locationRepository.requestLocationUpdates().collect { location ->
                fetchNearbyStations(location.latitude, location.longitude)
            }
        }
    }

    // ── 근처 역 계산 ──────────────────────────────────────────────
    private suspend fun fetchNearbyStations(lat: Double, lon: Double) {
        val result = withContext(Dispatchers.Default) {
            locationsCache
                .filter { abs(lat - it.latitude) < 0.04 && abs(lon - it.longitude) < 0.05 }
                .mapNotNull { loc ->
                    val d = haversine(lat, lon, loc.latitude, loc.longitude)
                    if (d <= 3.0) loc to d else null
                }
                .sortedBy { it.second }
                .flatMap { (loc, d) ->
                    stationNameMap[loc.stationName]?.map { info ->
                        NearbyStation(
                            stationName = info.stationName,
                            lineNumber = info.lineNumber,
                            distance = d,
                            latitude = loc.latitude,
                            longitude = loc.longitude
                        )
                    } ?: emptyList()
                }
                .distinctBy { it.stationName to it.lineNumber }
        }
        _nearbyStationList.value = result
    }

    // ── Haversine ─────────────────────────────────────────────────
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6_371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}