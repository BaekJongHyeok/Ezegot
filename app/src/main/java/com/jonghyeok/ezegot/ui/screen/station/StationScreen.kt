package com.jonghyeok.ezegot.ui.screen.station

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.ui.theme.BackgroundLight
import com.jonghyeok.ezegot.viewModel.StationViewModel

/**
 * 역 상세 화면.
 *
 * 실시간 도착 정보, 첫차·막차 시간표, 역 위치, 역 정보를 한 화면에 보여준다.
 * 구성 요소는 같은 패키지의 파일들로 나뉘어 있다.
 */
@Composable
fun StationScreen(
    stationName: String,
    lineNumber: String,
    viewModel: StationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onStationClick: (String, String) -> Unit
) {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val uiState by viewModel.uiState.collectAsState()

    val stationInfo = uiState.stationInfo
    val arrivalInfo = uiState.arrivals
    val isFavorite = uiState.isFavorite
    val stationLocation = uiState.stationLocation

    var showPermissionRationale by remember { mutableStateOf(false) }
    var pendingAlarmArrival by remember { mutableStateOf<RealtimeArrival?>(null) }
    var pendingAlarmThreshold by remember { mutableStateOf<Int?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한 허용됨 -> 보류 중인 알람 설정 실행
            pendingAlarmArrival?.let { arr ->
                pendingAlarmThreshold?.let { threshold ->
                    viewModel.setAlarm(arr, threshold)
                }
            }
        } else {
            // 권한 거부됨 -> 사용자에게 알림
            Toast.makeText(context, "알림 권한이 없으면 도착 정보를 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
        pendingAlarmArrival = null
        pendingAlarmThreshold = null
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("알림 권한 필요") },
            text = { Text("지하철 도착 알림을 받으려면 알림 권한 허용이 필요합니다. 다음 화면에서 '허용'을 선택해 주세요.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    pendingAlarmArrival = null
                    pendingAlarmThreshold = null
                }) {
                    Text("취소")
                }
            }
        )
    }

    val timeTable = uiState.timetable

    LaunchedEffect(stationLocation) {
        stationLocation?.let {
            viewModel.loadAdvancedStationInfo(stationName, lineNumber)
        }
    }

    LaunchedEffect(stationName) {
        viewModel.loadStationInfo(stationName, lineNumber)
        viewModel.loadArrivalInfo(stationName)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                val defaultLatLng = LatLng(loc?.latitude ?: 37.5665, loc?.longitude ?: 126.9780)
                viewModel.loadStationLocation(stationName, defaultLatLng)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        StationTopBar(
            stationName = stationName,
            lineNumber = lineNumber,
            arrivalInfo = arrivalInfo,
            isFavorite = isFavorite,
            onBack = onBack,
            onToggleFavorite = { viewModel.toggleFavorite() },
            onStationClick = onStationClick
        )

        // ── 액션 버튼 및 스크롤 영역 ────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            // 아래 스크롤 콘텐츠를 먼저 배치
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 34.dp) // Action 바 하단에 딱 붙도록 정밀 조정
            ) {
                // 도착 정보 카드
                stationInfo?.let { info ->
                    // 방향 레이블 미리 계산 (공통 사용)
                    val lineId = SubwayLine.getLineId(info.lineNumber) ?: ""
                    val defaultUp = if (info.lineNumber.contains("2호선")) "내선 방면" else "상행 방면"
                    val defaultDn = if (info.lineNumber.contains("2호선")) "외선 방면" else "하행 방면"

                    val upArr = arrivalInfo.filter {
                        it.subwayId == lineId && (it.updnLine == "상행" || it.updnLine == "내선") && it.getFormattedMessage() != "출발"
                    }.distinctBy { it.bstatnNm }

                    val dnArr = arrivalInfo.filter {
                        it.subwayId == lineId && (it.updnLine == "하행" || it.updnLine == "외선") && it.getFormattedMessage() != "출발"
                    }.distinctBy { it.bstatnNm }

                    val upDtLabel = upArr.firstOrNull()?.trainLineName?.split("-")?.lastOrNull()?.trim()
                        ?.replace("(급행)", "")?.trim() ?: defaultUp
                    val dnDtLabel = dnArr.firstOrNull()?.trainLineName?.split("-")?.lastOrNull()?.trim()
                         ?.replace("(급행)", "")?.trim() ?: defaultDn

                    ArrivalInfoSection(
                        arrivals = arrivalInfo,
                        line = info.lineNumber,
                        upDt = upDtLabel,
                        dnDt = dnDtLabel,
                        timeTable = timeTable,
                        activeAlarms = uiState.activeAlarms,
                        onSetAlarm = { arr, threshold ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                when {
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                                        viewModel.setAlarm(arr, threshold)
                                    }
                                    else -> {
                                        pendingAlarmArrival = arr
                                        pendingAlarmThreshold = threshold
                                        showPermissionRationale = true
                                    }
                                }
                            } else {
                                viewModel.setAlarm(arr, threshold)
                            }
                        },
                        onCancelAlarm = { trainNo -> viewModel.cancelAlarm(trainNo, stationName) }
                    )
                    Spacer(Modifier.height(16.dp))

                    // 첫차 / 막차 시간표 (로딩 중에도 카드 틀은 유지)
                    val (upTable, dnTable) = timeTable ?: Pair(null, null)
                    StationTimeTableCard(upTable, dnTable, upDtLabel, dnDtLabel, uiState.errorMessage)
                    Spacer(Modifier.height(16.dp))
                }

                // 지도
                stationLocation?.let { loc ->
                    StationMapCard(loc)
                }
                Spacer(Modifier.height(16.dp))

                // 역 정보
                StationInfoCard(address = stationLocation?.address ?: "주소 정보 없음")
                Spacer(Modifier.height(32.dp))
            }

            // 액션 버튼바를 위에 띄워서 오버랩 시킴 (공백 제거 효과)
            StationActionBar(
                isFavorite = isFavorite,
                isNotification = uiState.isNotification,
                viewModel = viewModel,
                stationInfo = stationInfo?.let { BasicStationInfo(it.stationName, it.lineNumber) },
                stationLocation = stationLocation,
                context = context,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-32).dp) // 위쪽으로 더 깊게 끌어올려 자연스럽게 오버랩
            )
        }
    }
}
