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
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.MarkerComposable
import com.jonghyeok.ezegot.R
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.NearbyStation
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.ui.theme.*
import com.jonghyeok.ezegot.viewModel.MainViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

// ── 탭 정의 ──────────────────────────────────────────────────────
internal enum class HomeTab(val label: String) { FAVORITE("즐겨찾기"), NEARBY("근처 역") }

@Composable
fun HomeScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onSearchClick: () -> Unit,
    onMapClick: () -> Unit,
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
            .background(BackgroundLight)
    ) {
        // ── ① 고정 상단 헤더 (항상 보임) ─────────────────────────
        StickyHeader(
            onSearchClick = onSearchClick,
            onMapClick = onMapClick,
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
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Navy900, Navy800)))
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
                color = TextOnDark,
                fontWeight = FontWeight.Black
            )
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "설정", tint = TextOnDark.copy(alpha = 0.6f))
            }
        }

        Spacer(Modifier.height(10.dp))

        // 검색바 (항상 visible)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(14.dp),
            color = Navy700
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
                    color = TextHint
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextOnDark.copy(alpha = 0.5f),
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
            .background(Navy900)
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
                    color = if (isSelected) SkyBlue400 else TextOnDark.copy(alpha = 0.4f)
                )
            }
            if (isSelected) {
                // 선택 탭 하단 인디케이터는 Row 레이아웃 특성상
                // 별도 오버레이 없이 색상 차이로만 표현 (심플하고 성능 좋음)
            }
        }
    }

    // 탭 하단 선택 인디케이터
    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Navy700)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .offset(x = if (selectedTab == HomeTab.FAVORITE) 0.dp else with(LocalDensity) { 0.dp })
                .background(SkyBlue400)
                .align(if (selectedTab == HomeTab.FAVORITE) Alignment.CenterStart else Alignment.CenterEnd)
        )
    }
}

// Density 접근을 위한 임시 helper
private val LocalDensity get() = androidx.compose.ui.platform.LocalDensity

// ── 즐겨찾기 탭 ──────────────────────────────────────────────────
@Composable
fun FavoriteTab(viewModel: MainViewModel, onStationClick: (String, String) -> Unit) {
    val favorites by viewModel.favoriteStationList.collectAsState()
    val arrivalMap by viewModel.realtimeArrivalInfo.collectAsState()
    val loadingStates by viewModel.loadingStates.collectAsState()
    val context = LocalContext.current

    var currentTime by remember { mutableStateOf(LocalTime.now()) }
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
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "실시간 도착 정보",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.clickable {
                        currentTime = LocalTime.now()
                        viewModel.loadRealtimeArrival()
                        rotation += 360f
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(currentTime.format(formatter), style = MaterialTheme.typography.labelMedium, color = TextHint)
                    Icon(
                        Icons.Default.Refresh, contentDescription = "새로고침", tint = TextHint,
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
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        color = SurfaceWhite
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
                    Text(
                        text = station.stationName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    val lineColor = getSubwayLineColor(station.lineNumber)
                    Surface(shape = RoundedCornerShape(6.dp), color = lineColor) {
                        Text(
                            text = station.lineNumber.removePrefix("0"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("›", style = MaterialTheme.typography.titleMedium, color = TextHint)
            }

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                // 스켈레톤
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (i == 1) 0.6f else 1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(DividerColor)
                    )
                    if (i < 2) Spacer(Modifier.height(8.dp))
                }
            } else {
                // 상행 / 하행 가로 2열 배치
                val upDest = upArrivals.firstOrNull()?.bstatnNm ?: "-"
                val dnDest = dnArrivals.firstOrNull()?.bstatnNm ?: "-"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ArrivalColumn(
                        modifier = Modifier.weight(1f),
                        direction = "↑ $upDest",
                        arrivals = upArrivals
                    )
                    Box(modifier = Modifier.width(1.dp).height(60.dp).background(DividerColor))
                    ArrivalColumn(
                        modifier = Modifier.weight(1f),
                        direction = "↓ $dnDest",
                        arrivals = dnArrivals
                    )
                }
            }
        }
    }
}

@Composable
fun ArrivalColumn(modifier: Modifier, direction: String, arrivals: List<RealtimeArrival>) {
    Column(modifier = modifier) {
        Text(
            text = direction,
            style = MaterialTheme.typography.labelSmall,
            color = TextHint,
            maxLines = 1,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        arrivals.take(2).forEach { a ->
            Text(
                text = a.getFormattedMessage(),
                style = MaterialTheme.typography.bodySmall,
                color = ArrivalRed,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (arrivals.isEmpty()) {
            Text("정보없음", style = MaterialTheme.typography.bodySmall, color = TextHint)
        }
    }
}

@Composable
fun EmptyFavoriteView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextHint, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(10.dp))
            Text("즐겨찾기를 추가해보세요", style = MaterialTheme.typography.bodyMedium, color = TextHint)
            Spacer(Modifier.height(4.dp))
            Text("역 상세에서 ★을 눌러 추가할 수 있어요", style = MaterialTheme.typography.bodySmall, color = TextHint)
        }
    }
}

// ── KTX 배너 (최소화) ─────────────────────────────────────────────
@Composable
fun KtxBannerMinimal(context: Context) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Navy900.copy(alpha = 0.07f))
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
            Text("🚄  기차 / KTX 승차권 예매", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text("바로가기 →", style = MaterialTheme.typography.labelSmall, color = SkyBlue400)
        }
    }
}

// ── 근처 역 탭 ───────────────────────────────────────────────────
@Composable
fun NearbyTab(viewModel: MainViewModel, onStationClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val nearbyStations by viewModel.nearbyStationList.collectAsState()

    val isPermissionGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    if (!isPermissionGranted || !isGpsOn) {
        LocationGuideCard(isPermissionGranted)
        return
    }

    val cameraPositionState = rememberCameraPositionState()
    var selectedStation by remember { mutableStateOf<NearbyStation?>(null) }
    var mapInitialized by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Map initial center logic
    LaunchedEffect(nearbyStations) {
        if (!mapInitialized && nearbyStations.isNotEmpty()) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    loc?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                        mapInitialized = true
                    } ?: run {
                        // Fallback: Default to first station if lastLocation is null
                        val firstLoc = LatLng(nearbyStations.first().latitude, nearbyStations.first().longitude)
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(firstLoc, 14f))
                        mapInitialized = true
                    }
                }
            } catch (e: SecurityException) { }
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
                    color = if (isSelected) lineColor else SurfaceWhite,
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
                                .background(if (isSelected) SurfaceWhite else lineColor)
                        )
                        Text(
                            text = "${station.stationName} ${station.lineNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) SurfaceWhite else TextPrimary,
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
                        .clickable { onStationClick(station.stationName, station.lineNumber) },
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color = SurfaceWhite
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
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(shape = RoundedCornerShape(6.dp), color = lineColor) {
                                        Text(
                                            text = station.lineNumber,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = String.format("내 위치에서 %.1f km", station.distance),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Navy900
                        ) {
                            Text(
                                text = "상세 정보",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
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
                .background(Color.White, androidx.compose.foundation.shape.CircleShape)
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
                    contentDescription = null,
                    modifier = Modifier.size(baseSize * 0.5f),
                    tint = Navy900
                )
            } else {
                // Single line icon or dot
                Box(
                    modifier = Modifier
                        .size(baseSize * 0.3f)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(lineColors.firstOrNull() ?: SkyBlue400)
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
                color = SurfaceWhite.copy(alpha = 0.9f),
                border = borderStroke,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = stationName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
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
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkyBlue400, modifier = Modifier.size(36.dp))
                Text(
                    text = if (!isPermissionGranted) "위치 권한 필요" else "GPS를 켜주세요",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (!isPermissionGranted) "근처 역을 보려면 위치 권한을 허용해 주세요"
                           else "GPS를 활성화하면 주변 역 정보를 확인할 수 있어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SkyBlue400
                ) {
                    Text(
                        text = "설정으로 이동",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
