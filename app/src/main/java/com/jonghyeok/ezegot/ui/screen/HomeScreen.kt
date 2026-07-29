package com.jonghyeok.ezegot.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.NearbyStation
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor
import com.jonghyeok.ezegot.ui.theme.onSubwayLineColor
import com.jonghyeok.ezegot.viewModel.LocationState
import com.jonghyeok.ezegot.viewModel.MainViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ── 탭 정의 ──────────────────────────────────────────────────────
internal enum class HomeTab(val label: String) { FAVORITE("즐겨찾기"), NEARBY("근처 역") }

@Composable
fun HomeScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onSearchClick: () -> Unit,
    onStationClick: (String, String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadRealtimeArrival()
        viewModel.updateCurrentLocation()
    }

    var selectedTab by remember { mutableStateOf(HomeTab.FAVORITE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── ① 고정 상단 헤더 (항상 보임) ─────────────────────────
        StickyHeader(
            onSearchClick = onSearchClick,
            onSettingsClick = {}
        )

        // ── ② 탭 바 ───────────────────────────────────────────────
        HomeTabBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        // ── ③ 탭 콘텐츠 (스크롤) ─────────────────────────────────
        when (selectedTab) {
            HomeTab.FAVORITE -> FavoriteTab(viewModel, onStationClick)
            HomeTab.NEARBY   -> NearbyTab(viewModel, onStationClick)
        }
    }
}

// ── 고정 헤더 ────────────────────────────────────────────────────
@Composable
fun StickyHeader(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 앱 이름 + 설정
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EZEGOT",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "설정", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(10.dp))

        // 검색바 (항상 visible)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)                       // 검색·지도 화면의 검색바와 동일
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    onClickLabel = "검색 화면 열기",
                    role = Role.Button
                ) { onSearchClick() },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "지하철 역 이름 검색",
                    style = MaterialTheme.typography.bodyMedium,
                    // 검색 필드는 surfaceVariant 위에 있으므로 보조 텍스트색을 쓴다
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── 탭 바 ────────────────────────────────────────────────────────
@Composable
internal fun HomeTabBar(selectedTab: HomeTab, onTabSelected: (HomeTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 탭 하단 선택 인디케이터 – 선택된 탭 쪽으로 정렬만 바꿔 표현
    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.outline)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary)
                .align(if (selectedTab == HomeTab.FAVORITE) Alignment.CenterStart else Alignment.CenterEnd)
        )
    }
}

// ── 즐겨찾기 탭 ──────────────────────────────────────────────────
@Composable
fun FavoriteTab(viewModel: MainViewModel, onStationClick: (String, String) -> Unit) {
    val favorites by viewModel.favoriteStationList.collectAsState()
    val arrivalMap by viewModel.realtimeArrivalInfo.collectAsState()
    val loadingStates by viewModel.loadingStates.collectAsState()
    val context = LocalContext.current

    // 갱신 시각은 ViewModel이 응답 반영 시점에 기록한 값을 그대로 쓴다
    val lastUpdatedAt by viewModel.lastUpdatedAt.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("a h:mm")
    var rotation by remember { mutableStateOf(0f) }
    val animRotation by animateFloatAsState(rotation, tween(500), label = "r")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // 섹션 헤더 (갱신 시각 + 리프레시)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),   // 아래 카드와 좌측 정렬을 맞춘다
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "실시간 도착 정보",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    // 텍스트 높이(약 16dp)만으로는 누르기 어려워 최소 48dp를 확보한다
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.loadRealtimeArrival()
                            rotation += 360f
                        }
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 아직 한 번도 데이터가 들어오지 않았으면 시각을 보여주지 않는다
                    lastUpdatedAt?.let {
                        Text(it.format(formatter), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(
                        Icons.Default.Refresh, contentDescription = "새로고침", tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp).graphicsLayer(rotationZ = animRotation)
                    )
                }
            }
        }

        if (favorites.isEmpty()) {
            item { EmptyFavoriteView() }
        } else {
            items(favorites) { station ->
                val isLoading = loadingStates[station.stationName] ?: true
                val lineId = SubwayLine.getLineId(station.lineNumber)
                val arrivals = arrivalMap[station.stationName] ?: emptyList()
                val up = arrivals.filter { it.subwayId == lineId && it.updnLine == "상행" }
                    .distinctBy { it.bstatnNm }.take(2)
                val dn = arrivals.filter { it.subwayId == lineId && it.updnLine != "상행" }
                    .distinctBy { it.bstatnNm }.take(2)
                FavoriteArrivalRow(
                    station = station,
                    upArrivals = up,
                    dnArrivals = dn,
                    isLoading = isLoading,
                    onClick = { onStationClick(station.stationName, station.lineNumber) }
                )
            }
        }

        // KTX 배너 → 하단에 subtle 삽입 (흐름 방해 X)
        item {
            Spacer(Modifier.height(8.dp))
            KtxBannerMinimal(context)
        }
    }
}

// ── 리스트형 즐겨찾기 카드 (가로스크롤 → 세로 리스트로 전환) ────
@Composable
fun FavoriteArrivalRow(
    station: BasicStationInfo,
    upArrivals: List<RealtimeArrival>,
    dnArrivals: List<RealtimeArrival>,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))   // ripple이 카드 모서리를 따르도록
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 역명 + 호선 뱃지
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 도착 시간보다 무게를 낮춘다. 사용자가 등록한 역이라 재확인 가치가 낮다
                    Text(
                        text = station.stationName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val lineColor = getSubwayLineColor(station.lineNumber)
                    Surface(shape = RoundedCornerShape(6.dp), color = lineColor) {
                        Text(
                            text = station.lineNumber.removePrefix("0"),
                            style = MaterialTheme.typography.labelSmall,
                            color = onSubwayLineColor(lineColor),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))

            // 도착 1건 = 1행. 건수만큼만 그리므로 카드 높이가 내용에 따라 줄어든다.
            val lines = upArrivals.take(2) + dnArrivals.take(2)

            when {
                isLoading -> {
                    // 스켈레톤도 실제 행 높이(28dp)에 맞춰 두 줄만 놓는다
                    repeat(2) { i ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (i == 0) 1f else 0.7f)
                                .height(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        if (i == 0) Spacer(Modifier.height(8.dp))
                    }
                }

                lines.isEmpty() -> {
                    Text(
                        text = "도착 정보 없음",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lines.forEach { ArrivalLine(it) }
                    }
                }
            }
        }
    }
}

/**
 * 도착 한 건을 한 줄로 보여준다.
 *
 * 좌측은 방면·행선지(보조), 우측은 남은 시간(주 정보).
 * 시간을 우측에 정렬해 여러 역을 세로로 훑을 때 눈이 한 열만 따라가면 되게 했다.
 */
@Composable
private fun ArrivalLine(arrival: RealtimeArrival) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = arrival.directionLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(
            text = arrival.getFormattedMessage(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/**
 * "성수행 - 교대방면" → "교대 방면 · 성수행"
 *
 * `trainLineNm`의 뒷부분은 다음 역(방면), `bstatnNm`은 종착역이다.
 * 종착역에 "행"을 붙여 방면과 구분되게 한다. 2호선처럼 순환하는 노선에서는
 * 방면과 종착역이 달라 보여서, 라벨이 없으면 서로 어긋난 정보처럼 읽힌다.
 */
private fun RealtimeArrival.directionLabel(): String {
    val via = trainLineName.substringAfter("-", "").replace("방면", "").trim()
    val dest = bstatnNm.trim()
    return when {
        via.isNotEmpty() && dest.isNotEmpty() -> "$via 방면 · ${dest}행"
        dest.isNotEmpty() -> "${dest}행"
        via.isNotEmpty() -> "$via 방면"
        else -> "행선지 정보 없음"
    }
}

/**
 * 데이터가 없을 때 쓰는 공통 빈 상태.
 *
 * 아이콘·제목·설명 3단 구조를 화면 간에 동일하게 유지하려고 하나로 모았다.
 */
@Composable
internal fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier.fillMaxWidth().height(200.dp)
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyFavoriteView() {
    EmptyStateView(
        icon = Icons.Default.LocationOn,
        title = "즐겨찾기를 추가해보세요",
        description = "역 상세에서 ★을 눌러 추가할 수 있어요"
    )
}

// ── KTX 배너 (최소화) ─────────────────────────────────────────────
@Composable
fun KtxBannerMinimal(context: Context) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    Uri.parse("https://www.letskorail.com/")
                )
                context.startActivity(intent)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🚄  기차 / KTX 승차권 예매", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("바로가기 →", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ── 근처 역 탭 ───────────────────────────────────────────────────
@Composable
fun NearbyTab(viewModel: MainViewModel, onStationClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val nearbyStations by viewModel.nearbyStationList.collectAsState()
    val locationState by viewModel.locationState.collectAsState()

    val isPermissionGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    if (!isPermissionGranted || !isGpsOn) {
        LocationGuideCard(isPermissionGranted)
        return
    }

    // 권한·GPS는 정상인데 위치를 못 얻는 경우가 있다(캐시 없음 + 첫 fix 지연).
    // 기본 좌표 지도를 그대로 보여주면 실패가 드러나지 않으므로 상태로 표현한다.
    val available = locationState as? LocationState.Available
    if (available == null) {
        if (locationState is LocationState.Unavailable) {
            LocationUnavailableCard(onRetry = { viewModel.retryLocation() })
        } else {
            LocationLoadingCard()
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState()
    var selectedStation by remember { mutableStateOf<NearbyStation?>(null) }
    var mapInitialized by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 지도 초기 중심 – 위치를 얻는 즉시 이동한다.
    // 근처 역 목록을 기다리지 않는다. 목록이 비어도 현재 위치는 보여야 하기 때문이다.
    LaunchedEffect(available.latitude, available.longitude) {
        if (!mapInitialized) {
            val latLng = LatLng(available.latitude, available.longitude)
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
            mapInitialized = true
        }
    }

    val groupedStations = remember(nearbyStations) {
        nearbyStations.groupBy { it.latitude to it.longitude }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
        ) {
            groupedStations.forEach { (coords, stationsAtPos) ->
                val pos = LatLng(coords.first, coords.second)
                val lineColors = stationsAtPos.map { getSubwayLineColor(it.lineNumber) }
                val isAnySelected = stationsAtPos.any { it.stationName == selectedStation?.stationName && it.lineNumber == selectedStation?.lineNumber }

                MarkerComposable(
                    state = MarkerState(position = pos),
                    onClick = {
                        selectedStation = stationsAtPos.first()
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLng(pos))
                        }
                        true
                    }
                ) {
                    StationMarkerIcon(
                        lineColors = lineColors,
                        isSelected = isAnySelected,
                        stationName = stationsAtPos.first().stationName
                    )
                }
            }
        }

        // 반경 내 역이 없을 때. 지도(현재 위치)는 그대로 두고 안내만 얹는다.
        if (nearbyStations.isEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "근처에 지하철역이 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "현재 위치에서 3km 안에 표시할 역이 없어요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Top horizontal chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nearbyStations) { station ->
                val isSelected = selectedStation?.stationName == station.stationName && selectedStation?.lineNumber == station.lineNumber
                val lineColor = getSubwayLineColor(station.lineNumber)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) lineColor else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, lineColor),
                    shadowElevation = 4.dp,
                    modifier = Modifier.clickable {
                        selectedStation = station
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(station.latitude, station.longitude), 15f))
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (isSelected) onSubwayLineColor(lineColor) else lineColor)
                        )
                        Text(
                            text = "${station.stationName} ${station.lineNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) onSubwayLineColor(lineColor) else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom Card for selected station
        androidx.compose.animation.AnimatedVisibility(
            visible = selectedStation != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .padding(bottom = 24.dp), // Extra padding for Map watermark
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
        ) {
            selectedStation?.let { station ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))   // ripple이 카드 모서리를 따르도록
                        .clickable { onStationClick(station.stationName, station.lineNumber) },
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    val lineColor = getSubwayLineColor(station.lineNumber)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(lineColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = lineColor)
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = station.stationName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(shape = RoundedCornerShape(6.dp), color = lineColor) {
                                        Text(
                                            text = station.lineNumber,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onSubwayLineColor(lineColor),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = String.format("내 위치에서 %.1f km", station.distance),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "상세 정보",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StationMarkerIcon(lineColors: List<Color>, isSelected: Boolean, stationName: String? = null) {
    val baseSize = if (isSelected) 42.dp else 34.dp
    val strokeWidth = if (isSelected) 4.dp else 3.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(baseSize)
                .shadow(if (isSelected) 8.dp else 4.dp, androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape)
                .padding(strokeWidth / 2), // Space for stroke
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                if (lineColors.size == 1) {
                    drawCircle(
                        color = lineColors[0],
                        style = Stroke(width = strokeWidth.toPx())
                    )
                } else {
                    val sweepAngle = 360f / lineColors.size
                    lineColors.forEachIndexed { index, color ->
                        drawArc(
                            color = color,
                            startAngle = index * sweepAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx())
                        )
                    }
                }
            }
            
            if (lineColors.size > 1) {
                Icon(
                    imageVector = Icons.Default.SyncAlt,
                    // 환승역 여부가 이 아이콘 모양으로만 전달되고 있었다
                    contentDescription = "환승역",
                    modifier = Modifier.size(baseSize * 0.5f),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            } else {
                // Single line icon or dot
                Box(
                    modifier = Modifier
                        .size(baseSize * 0.3f)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(lineColors.firstOrNull() ?: MaterialTheme.colorScheme.primary)
                )
            }
        }

        if (stationName != null) {
            val borderStroke = if (lineColors.size == 1) {
                BorderStroke(1.dp, lineColors.first())
            } else {
                // 여러 노선일 경우 선형 그라데이션으로 섞어서 표현
                BorderStroke(1.dp, Brush.linearGradient(lineColors))
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surface,
                border = borderStroke,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = stationName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ── 위치 조회 상태 카드 ──────────────────────────────────────────
/** 위치를 확인하는 중. 권한·GPS는 정상이므로 안내 대신 진행 상황만 보여준다. */
@Composable
fun LocationLoadingCard() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Text(
                text = "위치를 확인하는 중",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 제한 시간 안에 위치를 못 얻은 경우. 재시도 수단을 함께 준다. */
@Composable
fun LocationUnavailableCard(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                Text(
                    text = "위치를 가져올 수 없습니다",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "실내에서는 신호가 약할 수 있어요. GPS가 켜져 있는지 확인해 주세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onRetry() }
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "다시 시도",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationGuideCard(isPermissionGranted: Boolean) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable {
                    val intent = if (!isPermissionGranted) {
                        android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    } else {
                        android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    }
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                Text(
                    text = if (!isPermissionGranted) "위치 권한 필요" else "GPS를 켜주세요",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (!isPermissionGranted) "근처 역을 보려면 위치 권한을 허용해 주세요"
                           else "GPS를 활성화하면 주변 역 정보를 확인할 수 있어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "설정으로 이동",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
