package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.FavoriteStation
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
import java.time.LocalTime
import kotlin.math.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val favoriteRepository: FavoriteRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    // ── 즐겨찾기 (방향 단위) ─────────────────────────────────────
    val favoriteStationList: StateFlow<List<FavoriteStation>> = favoriteRepository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── 실시간 도착 정보 (stationName → List) ─────────────────────
    private val _realtimeArrivalInfo = MutableStateFlow<Map<String, List<RealtimeArrival>>>(emptyMap())
    val realtimeArrivalInfo: StateFlow<Map<String, List<RealtimeArrival>>> = _realtimeArrivalInfo.asStateFlow()

    // ── 개별 카드 로딩 상태 (stationName → Boolean) ───────────────
    private val _loadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val loadingStates: StateFlow<Map<String, Boolean>> = _loadingStates.asStateFlow()

    /**
     * 도착 정보가 마지막으로 화면에 반영된 시각.
     *
     * 새로고침 버튼을 누른 시각이 아니라 응답이 실제로 들어온 시각이다.
     * 누른 시각을 쓰면 요청이 실패해도 시각만 갱신돼, 오래된 데이터가
     * 방금 갱신된 것처럼 보인다.
     */
    private val _lastUpdatedAt = MutableStateFlow<LocalTime?>(null)
    val lastUpdatedAt: StateFlow<LocalTime?> = _lastUpdatedAt.asStateFlow()

    // ── 근처 역 ──────────────────────────────────────────────────
    private val _nearbyStationList = MutableStateFlow<List<NearbyStation>>(emptyList())
    val nearbyStationList: StateFlow<List<NearbyStation>> = _nearbyStationList.asStateFlow()

    // ── 현재 위치 ────────────────────────────────────────────────
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Loading)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private var stationNameMap: Map<String, List<StationInfo>> = emptyMap()
    private var locationsCache: List<StationInfoResponse> = emptyList()

    // 새로고침 시 이전 Job 취소용
    private var arrivalJob: Job? = null

    // 위치 조회 Job. 중복 실행 방지 및 재시도 시 취소용
    private var locationJob: Job? = null

    // 초기 데이터(역 목록·위경도) 로드 Job. 근처 역 계산이 이 완료를 기다린다
    private var initialDataJob: Job? = null

    init {
        loadInitialData()
        observeFavoritesForAutoRefresh()
    }

    // ── 초기 데이터 로드 ─────────────────────────────────────────
    private fun loadInitialData() {
        initialDataJob = viewModelScope.launch {
            val stationsDeferred  = launch { mainRepository.getAllStations().let { s ->
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
                // 실시간 API는 역 단위라, 같은 역의 두 방향을 담았어도 한 번만 부른다.
                // 일일 1,000건 제한이 있어 중복 호출을 그대로 두면 안 된다.
                .map { list -> list.map { it.stationName }.distinct() }
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
        streamRealtimeArrival(favorites.map { it.stationName }.distinct())
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

                    // 실제 데이터가 들어온 경우에만 갱신 시각으로 인정한다.
                    // Repository가 실패 시 빈 값을 반환하므로 빈 응답은 실패와
                    // 구분되지 않는다. 이때 시각을 올리면 실패를 성공처럼 보이게 한다.
                    if (arrivals.isNotEmpty()) {
                        _lastUpdatedAt.value = LocalTime.now()
                    }
                }
            }
        }
    }

    // ── GPS – Two-phase 패턴 ──────────────────────────────────────
    /**
     * 현재 위치를 조회하고 [locationState]에 반영한다.
     *
     * Phase 1은 Repository가 담당한다(캐시된 위치 → 없으면 첫 fix 대기).
     * 제한 시간 안에 하나도 못 얻으면 [LocationState.Unavailable]로 확정해
     * 화면이 기본 좌표에 머무르는 대신 상태를 보여줄 수 있게 한다.
     * Phase 2는 이후 지속 갱신으로 더 정확한 위치를 반영한다.
     */
    fun updateCurrentLocation() {
        // 이미 조회 중이면 중복 실행하지 않는다 (탭 전환 시 재호출 방지)
        if (locationJob?.isActive == true) return

        locationJob = viewModelScope.launch {
            _locationState.value = LocationState.Loading

            val first = locationRepository.getCurrentLocation()
            if (first == null) {
                _locationState.value = LocationState.Unavailable
                return@launch
            }
            _locationState.value = LocationState.Available(first.latitude, first.longitude)
            fetchNearbyStations(first.latitude, first.longitude)

            // Phase 2: 지속 GPS 갱신
            locationRepository.requestLocationUpdates().collect { location ->
                _locationState.value = LocationState.Available(location.latitude, location.longitude)
                fetchNearbyStations(location.latitude, location.longitude)
            }
        }
    }

    /** 위치 조회 실패 후 사용자가 다시 시도할 때 호출한다. */
    fun retryLocation() {
        locationJob?.cancel()
        locationJob = null
        updateCurrentLocation()
    }

    // ── 근처 역 계산 ──────────────────────────────────────────────
    private suspend fun fetchNearbyStations(lat: Double, lon: Double) {
        // 위경도 목록이 아직 로드 전이면 빈 결과가 나오므로 완료를 기다린다
        initialDataJob?.join()

        val result = withContext(Dispatchers.Default) {
            locationsCache
                .filter { abs(lat - it.latitude) < 0.04 && abs(lon - it.longitude) < 0.05 }
                .mapNotNull { loc ->
                    val d = haversine(lat, lon, loc.latitude, loc.longitude)
                    // 3km는 도보 40분 거리라 "주변"으로 읽히지 않는다.
                    // 1.5km(도보 약 22분)까지만 남긴다.
                    if (d <= NEARBY_RADIUS_KM) loc to d else null
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

    companion object {
        /** 근처 역 반경(km). 도보 약 22분 */
        const val NEARBY_RADIUS_KM = 1.5
    }
}