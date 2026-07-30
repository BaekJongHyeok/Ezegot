package com.jonghyeok.ezegot.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.jonghyeok.ezegot.dto.NearbyStation
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor
import com.jonghyeok.ezegot.ui.theme.onSubwayLineColor
import com.jonghyeok.ezegot.viewModel.LocationState
import com.jonghyeok.ezegot.viewModel.MainViewModel
import kotlinx.coroutines.launch

/**
 * 근처 역 지도. 홈의 두 번째 탭이던 것을 독립 화면으로 분리했다.
 */
@Composable
fun NearbyMapScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onStationClick: (String, String) -> Unit
) {
    LaunchedEffect(Unit) { viewModel.updateCurrentLocation() }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(title = "근처 역")
        NearbyMapBody(viewModel, onStationClick)
    }
}

@Composable
private fun NearbyMapBody(viewModel: MainViewModel, onStationClick: (String, String) -> Unit) {
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
                        fontWeight = FontWeight.Medium
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
                            fontWeight = FontWeight.Medium
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
                                        fontWeight = FontWeight.Medium
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
                    fontWeight = FontWeight.Medium,
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
                    fontWeight = FontWeight.Medium
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
                    fontWeight = FontWeight.Medium
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
