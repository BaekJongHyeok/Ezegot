package com.jonghyeok.ezegot.ui.screen.station

import com.jonghyeok.ezegot.util.forDisplay
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.jonghyeok.ezegot.ui.theme.subwayLineHeaderColors
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
    // 헤더는 배경을 어둡게 보정해 11sp까지 4.5:1을 맞춘다. 도착 카드의 점·뱃지는
    // 아래에서 원색(lineColor)을 그대로 쓴다 — 공식 색을 바꾸는 것은 헤더 배경뿐이다.
    val headerColors = subwayLineHeaderColors(lineColor)
    val lineId = SubwayLine.getLineId(lineNumber)
    val (upDirection, dnDirection) = directionPairFor(lineNumber)

    // 방향별 도착 목록. 거르기와 정렬은 위젯과 공유한다(ArrivalOrdering).
    //
    // 종착역으로 중복을 지우면 안 된다. 2호선 내선은 다음 두 대가 모두 "성수행"이라
    // 열차가 2대 와도 1대만 남았다. 같은 열차가 두 번 오지는 않으므로 그대로 쓴다.
    fun arrivalsOf(direction: String) =
        uiState.arrivals.forDisplay(lineId) { it.matchesDirection(direction) }

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
    var showTimetable by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "알림 권한이 없으면 도착 정보를 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
            alarmTarget = null
        }
    }

    // ── 알람 예약 다이얼로그 ─────────────────────────────────────
    //
    // 선택지 세 개를 confirmButton에 넣으면 안 된다. AlertDialog는 confirmButton과
    // dismissButton을 한 줄에 배치하는데, fillMaxWidth 버튼이 그 줄을 다 차지해
    // "취소"가 "3분 전" 버튼 위에 겹쳐 찍혔다. 선택지는 본문 슬롯으로 내리고
    // 액션 줄에는 취소 하나만 남긴다.
    alarmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { alarmTarget = null },
            title = { Text("도착 알림 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("열차 도착 몇 분 전에 알림을 받을까요?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
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
            confirmButton = {
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
    // 예전에는 upDirection으로 고정해 열어서, 어느 방향인지 모른 채 열리고
    // 반대 방향은 볼 방법이 아예 없었다. 이제 시트 안에서 전환한다.
    if (showTimetable) {
        ModalBottomSheet(
            onDismissRequest = { showTimetable = false },
            // 중간 단계를 두면 내용이 길어 절반만 열린 채 멈춘다.
            // 시간표는 처음부터 다 펼쳐 보이는 편이 낫다.
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            StationTimetableSheet(
                stationName = stationName,
                upLabel = upLabel,
                dnLabel = dnLabel,
                upSchedules = uiState.timetable?.first?.schedules ?: emptyList(),
                dnSchedules = uiState.timetable?.second?.schedules ?: emptyList(),
                startWithUp = true,
                onClose = { showTimetable = false }
            )
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

    /**
     * 이 방향에 걸려 있는 예약.
     *
     * 열차 번호만 비교하면 같은 열차가 지나는 다른 역을 열었을 때도 종이 켜져 보였다.
     * 예약은 역 단위로 저장하므로 역 이름까지 함께 본다.
     */
    fun activeAlarmFor(arrivals: List<RealtimeArrival>) =
        uiState.activeAlarms.firstOrNull { alarm ->
            alarm.stationName == stationName && arrivals.any { it.trainNumber == alarm.trainNo }
        }

    /**
     * 종 아이콘을 누르면 켜고 끈다.
     *
     * 예전에는 상태와 관계없이 예약 다이얼로그만 띄웠다. 이미 켜진 종을 눌러도
     * 같은 다이얼로그가 다시 떴고, scheduleAlarm이 기존 예약을 보고 그대로
     * 돌아가 아무 일도 일어나지 않았다. 아이콘은 "알림 해제"라고 읽어주는데
     * 해제할 방법이 역 상세에 없어 알림 탭까지 가야 했다.
     */
    fun toggleAlarm(arrivals: List<RealtimeArrival>) {
        val active = activeAlarmFor(arrivals)
        if (active != null) {
            viewModel.cancelAlarm(active.trainNo, active.stationName)
        } else {
            requestAlarm(arrivals)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StationHeader(
            stationName = stationName,
            lineNumber = lineNumber,
            colors = headerColors,
            isFavorite = uiState.isFavorite,
            transferLines = transferLines,
            onBack = onBack,
            onToggleFavorite = { viewModel.toggleFavorite() },
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
                isAlarmOn = activeAlarmFor(upArrivals) != null,
                onAlarmClick = { toggleAlarm(upArrivals) }
            )
            ArrivalDirectionCard(
                directionLabel = dnLabel,
                lineColor = lineColor,
                arrivals = dnArrivals,
                isAlarmOn = activeAlarmFor(dnArrivals) != null,
                onAlarmClick = { toggleAlarm(dnArrivals) }
            )

            StationFirstLastSection(
                lineNumber = lineNumber,
                upLabel = upLabel,
                dnLabel = dnLabel,
                up = uiState.timetable?.first,
                down = uiState.timetable?.second,
                errorMessage = uiState.errorMessage,
                onOpenFullTimetable = { showTimetable = true }
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
