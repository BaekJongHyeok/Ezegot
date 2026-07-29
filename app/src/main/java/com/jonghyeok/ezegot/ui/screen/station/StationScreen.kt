package com.jonghyeok.ezegot.ui.screen.station

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.dto.directionPairFor
import com.jonghyeok.ezegot.dto.matchesDirection
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor
import com.jonghyeok.ezegot.ui.theme.onSubwayLineColorLarge
import com.jonghyeok.ezegot.viewModel.StationViewModel

/**
 * 역 상세.
 *
 * 정보를 탭으로 감추지 않고 세로로 전부 노출한다.
 * 위에서부터 헤더 → 방향별 도착 카드 → 첫차·막차 → 위치.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationScreen(
    stationName: String,
    lineNumber: String,
    viewModel: StationViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onStationClick: (String, String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(stationName) {
        viewModel.loadStationInfo(stationName, lineNumber)
        viewModel.loadArrivalInfo(stationName)
        viewModel.loadAdvancedStationInfo(stationName, lineNumber)
        viewModel.loadStationLocation(stationName)
    }

    val lineColor = getSubwayLineColor(lineNumber)
    val onLine = onSubwayLineColorLarge(lineColor)
    val lineId = SubwayLine.getLineId(lineNumber)
    val (upDirection, dnDirection) = directionPairFor(lineNumber)

    // 방향별 도착 목록. "출발"한 열차는 이미 떠났으므로 뺀다.
    //
    // 종착역으로 중복을 지우면 안 된다. 2호선 내선은 다음 두 대가 모두 "성수행"이라
    // 열차가 2대 와도 1대만 남았다. 같은 열차가 두 번 오지는 않으므로 그대로 쓴다.
    fun arrivalsOf(direction: String) = uiState.arrivals
        .filter {
            it.subwayId == lineId &&
                it.updnLine.matchesDirection(direction) &&
                it.getFormattedMessage() != "출발"
        }

    val upArrivals = arrivalsOf(upDirection)
    val dnArrivals = arrivalsOf(dnDirection)

    // 방면 라벨은 실시간 trainLineNm의 뒷부분(다음 역)에서 뽑는다.
    // 시간표의 종착역을 쓰면 2호선처럼 순환하는 노선에서 좌우가 똑같아진다.
    val upLabel = upArrivals.directionLabel(upDirection)
    val dnLabel = dnArrivals.directionLabel(dnDirection)

    val transferLines = remember(uiState.arrivals) {
        uiState.arrivals.firstOrNull()?.subwayList
            ?.split(",")
            ?.filter { it.isNotBlank() && it != lineId }
            ?.mapNotNull { SubwayLine.getLineName(it) }
            ?: emptyList()
    }

    var alarmTarget by remember { mutableStateOf<RealtimeArrival?>(null) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var sheetDirection by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "알림 권한이 없으면 도착 정보를 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
            alarmTarget = null
        }
    }

    // ── 알람 예약 다이얼로그 ─────────────────────────────────────
    alarmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { alarmTarget = null },
            title = { Text("도착 알림 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium) },
            text = { Text("열차 도착 몇 분 전에 알림을 받을까요?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 5).forEach { min ->
                        Button(
                            onClick = {
                                viewModel.setAlarm(target, min)
                                alarmTarget = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("${min}분 전") }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { alarmTarget = null }) {
                    Text("취소", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("알림 권한 필요") },
            text = { Text("지하철 도착 알림을 받으려면 알림 권한 허용이 필요합니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false; alarmTarget = null }) { Text("취소") }
            }
        )
    }

    // ── 전체 시간표 시트 ─────────────────────────────────────────
    sheetDirection?.let { direction ->
        val schedules = if (direction == upDirection) {
            uiState.timetable?.first?.schedules ?: emptyList()
        } else {
            uiState.timetable?.second?.schedules ?: emptyList()
        }
        ModalBottomSheet(
            onDismissRequest = { sheetDirection = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            FullTimetableSheet(
                direction = if (direction == upDirection) upLabel else dnLabel,
                schedules = schedules
            ) { sheetDirection = null }
        }
    }

    /** 알림 아이콘: 그 방향의 가장 빠른 열차로 예약한다 */
    fun requestAlarm(arrivals: List<RealtimeArrival>) {
        val target = arrivals.firstOrNull { it.trainNumber.isNotEmpty() } ?: return
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) alarmTarget = target else { alarmTarget = target; showPermissionRationale = true }
    }

    fun isAlarmOn(arrivals: List<RealtimeArrival>) =
        arrivals.any { a -> uiState.activeAlarms.any { it.trainNo == a.trainNumber } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StationHeader(
            stationName = stationName,
            lineNumber = lineNumber,
            lineColor = lineColor,
            onLine = onLine,
            isAnyDirectionFavorite = uiState.favoriteDirections.isNotEmpty(),
            transferLines = transferLines,
            onBack = onBack,
            onToggleFavorite = { viewModel.toggleFavoriteDirection(upDirection) },
            onCall = { context.dial() },
            onShare = { context.shareStation(stationName, lineNumber, uiState.stationLocation?.address) },
            onTransferClick = { name -> onStationClick(stationName, name) }
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ArrivalDirectionCard(
                directionLabel = upLabel,
                lineColor = lineColor,
                arrivals = upArrivals,
                isAlarmOn = isAlarmOn(upArrivals),
                onAlarmClick = { requestAlarm(upArrivals) }
            )
            ArrivalDirectionCard(
                directionLabel = dnLabel,
                lineColor = lineColor,
                arrivals = dnArrivals,
                isAlarmOn = isAlarmOn(dnArrivals),
                onAlarmClick = { requestAlarm(dnArrivals) }
            )

            StationFirstLastSection(
                upLabel = upLabel,
                dnLabel = dnLabel,
                up = uiState.timetable?.first,
                down = uiState.timetable?.second,
                errorMessage = uiState.errorMessage,
                onOpenFullTimetable = { sheetDirection = upDirection }
            )

            StationLocationCard(location = uiState.stationLocation)

            // 마지막 카드가 화면 끝에 붙어 잘려 보이지 않도록
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * 이 방향의 방면 라벨. `trainLineNm`의 "-" 뒷부분이 다음 역이다.
 * 값이 없으면 방향 표기(상행/내선)로 되돌린다.
 */
private fun List<RealtimeArrival>.directionLabel(fallback: String): String =
    firstOrNull()?.trainLineName
        ?.substringAfter("-", "")
        ?.replace("방면", "")
        ?.replace("(급행)", "")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: fallback

private fun Context.dial() {
    startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:15447788") })
}

private fun Context.shareStation(stationName: String, lineNumber: String, address: String?) {
    val text = buildString {
        appendLine("[Ezegot - 지하철 정보 도우미]")
        appendLine("🚇 $lineNumber ${stationName}역")
        if (!address.isNullOrBlank()) {
            appendLine()
            appendLine("📍 위치: $address")
        }
    }.trim()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(Intent.createChooser(intent, "공유하기"))
}
