package com.jonghyeok.ezegot.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.ui.theme.*
import com.jonghyeok.ezegot.viewModel.StationViewModel

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
    val stationInfo by viewModel.stationInfo.collectAsState()
    val arrivalInfo by viewModel.arrivalInfo.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isNotification by viewModel.isNotification.collectAsState()
    val stationLocation by viewModel.stationLocation.collectAsState()
    
    // 고급 기능 State
    val timeTable by viewModel.timeTable.collectAsState()
    val facilityInfo by viewModel.facilityInfo.collectAsState()

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
        // ── Top Bar ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Navy900, Navy800)))
        ) {
            Column(modifier = Modifier.padding(bottom = 48.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = TextOnDark)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "즐겨찾기",
                            tint = if (isFavorite) SkyBlue400 else TextOnDark.copy(alpha = 0.6f)
                        )
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stationName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextOnDark,
                            fontWeight = FontWeight.Bold
                        )
                        val lineColor = getSubwayLineColor(lineNumber)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = lineColor
                        ) {
                            Text(
                                text = lineNumber.removePrefix("0"),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    // 환승 정보
                    val currentLineId = SubwayLine.getLineId(lineNumber)
                    val transferLines = remember(arrivalInfo) {
                        arrivalInfo.firstOrNull()?.subwayList?.split(",")?.filter { it.isNotBlank() && it != currentLineId } ?: emptyList<String>()
                    }
                    if (transferLines.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "환승 노선",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextOnDark.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                transferLines.forEach { id ->
                                    val lineImageRes = SubwayLine.getLineImageById(id)
                                    val name = SubwayLine.getLineName(id) ?: id
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = lineImageRes),
                                        contentDescription = name,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { onStationClick(stationName, name) }
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

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

                    ArrivalInfoSection(arrivalInfo, info.lineNumber, upDtLabel, dnDtLabel, timeTable)
                    Spacer(Modifier.height(16.dp))

                    // 첫차 / 막차 시간표 (로딩 중에도 카드 틀은 유지)
                    val (upTable, dnTable) = timeTable ?: Pair(null, null)
                    StationTimeTableCard(upTable, dnTable, upDtLabel, dnDtLabel)
                    Spacer(Modifier.height(16.dp))
                }

                // 지도
                stationLocation?.let { loc ->
                    StationMapCard(loc)
                }
                Spacer(Modifier.height(16.dp))

                // 역 정보 및 편의 시설
                StationInfoCard(
                    address = stationLocation?.address ?: "주소 정보 없음",
                    facility = facilityInfo
                )
                Spacer(Modifier.height(32.dp))
            }

            // 액션 버튼바를 위에 띄워서 오버랩 시킴 (공백 제거 효과)
            StationActionBar(
                isFavorite = isFavorite,
                isNotification = isNotification,
                viewModel = viewModel,
                stationInfo = stationInfo?.let { com.jonghyeok.ezegot.dto.BasicStationInfo(it.stationName, it.lineNumber) },
                stationLocation = stationLocation,
                context = context,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-32).dp) // 위쪽으로 더 깊게 끌어올려 자연스럽게 오버랩
            )
        }
    }
}

@Composable
fun StationActionBar(
    isFavorite: Boolean,
    isNotification: Boolean,
    viewModel: StationViewModel,
    stationInfo: com.jonghyeok.ezegot.dto.BasicStationInfo?,
    stationLocation: StationInfoResponse?,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 알람
            ActionItem(
                icon = { Icon(if (isNotification) Icons.Default.Notifications else Icons.Default.NotificationsNone, null, tint = if (isNotification) SkyBlue400 else TextSecondary, modifier = Modifier.size(22.dp)) },
                label = "알람",
                onClick = { viewModel.toggleNotification() }
            )
            VerticalDivider(modifier = Modifier.height(32.dp), thickness = 1.dp, color = DividerColor)
            // 전화
            ActionItem(
                icon = { Icon(Icons.Default.Call, null, tint = TextSecondary, modifier = Modifier.size(22.dp)) },
                label = "전화",
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:15447788")
                    }
                    context.startActivity(intent)
                }
            )
            VerticalDivider(modifier = Modifier.height(32.dp), thickness = 1.dp, color = DividerColor)
            // 공유
            ActionItem(
                icon = { Icon(Icons.Default.Share, null, tint = TextSecondary, modifier = Modifier.size(22.dp)) },
                label = "공유",
                onClick = {
                    val stationName = stationInfo?.stationName ?: ""
                    val lineNumber = stationInfo?.lineNumber ?: ""
                    
                    val textBuilder = java.lang.StringBuilder().apply {
                        appendLine("[Ezegot - 지하철 정보 도우미]")
                        appendLine("🚇 $lineNumber ${stationName}역")
                        
                        stationLocation?.let { loc ->
                            appendLine()
                            val address = loc.address ?: "주소 정보 없음"
                            appendLine("📍 위치: $address")
                            appendLine("🗺️ 지도에서 확인하기: https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}")
                        }
                    }
                    val text = textBuilder.toString().trim()

                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, text)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "공유하기"))
                }
            )
        }
    }
}

@Composable
fun ActionItem(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun ArrivalInfoSection(
    arrivals: List<RealtimeArrival>, 
    line: String,
    upDt: String,
    dnDt: String,
    timeTable: Pair<com.jonghyeok.ezegot.api.TimeTableResponse?, com.jonghyeok.ezegot.api.TimeTableResponse?>?
) {
    var isRealtime by remember { mutableStateOf(true) }
    
    val lineId = SubwayLine.getLineId(line)
    
    val upList: List<RealtimeArrival>
    val dnList: List<RealtimeArrival>

    if (isRealtime) {
        upList = arrivals.filter { 
            it.subwayId == lineId && (it.updnLine == "상행" || it.updnLine == "내선") && it.getFormattedMessage() != "출발"
        }.distinctBy { it.bstatnNm }
        
        dnList = arrivals.filter { 
            it.subwayId == lineId && (it.updnLine == "하행" || it.updnLine == "외선") && it.getFormattedMessage() != "출발"
        }.distinctBy { it.bstatnNm }
    } else {
        // 시간표 모드
        val (upResp, dnResp) = timeTable ?: Pair(null, null)
        val extractedUp = getUpcomingTrainsFromTimeTable(upResp?.schedules ?: emptyList())
        val extractedDn = getUpcomingTrainsFromTimeTable(dnResp?.schedules ?: emptyList())
        
        upList = extractedUp
        dnList = extractedDn
    }
    
    // 전체 시간표 (시간표 모드에서 '시간표 보기' 버튼에도 사용)
    val (upRsp, dnRsp) = timeTable ?: Pair(null, null)
    val upFullSchedules = upRsp?.schedules ?: emptyList()
    val dnFullSchedules = dnRsp?.schedules ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth()) {
        // 섹션 타이틀 + 실시간/시간표 스위치
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "열차 도착 정보",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "실시간",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isRealtime) Navy800 else TextHint,
                    fontWeight = if (isRealtime) FontWeight.Bold else FontWeight.Normal
                )
                androidx.compose.material3.Switch(
                    checked = !isRealtime,
                    onCheckedChange = { isRealtime = !it },
                    modifier = Modifier.padding(horizontal = 6.dp).scale(0.75f),
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Navy800,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = TextHint
                    )
                )
                Text(
                    text = "시간표",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (!isRealtime) Navy800 else TextHint,
                    fontWeight = if (!isRealtime) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ArrivalCard(Modifier.weight(1f), upList, upDt, upFullSchedules)
            ArrivalCard(Modifier.weight(1f), dnList, dnDt, dnFullSchedules)
        }
    }
}

/** 
 * 전체 시간표 목록에서 현재 시점 이후로 가장 빨리 도착할 2대의 열차를 추려내어 RealtimeArrival 객체로 변환 (UI 호환용)
 */
fun getUpcomingTrainsFromTimeTable(schedules: List<com.jonghyeok.ezegot.api.TimeTableSchedule>): List<RealtimeArrival> {
    if (schedules.isEmpty()) return emptyList()
    
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.KOREAN)
    val now = java.util.Calendar.getInstance()
    // 현재 시간을 HH:mm:ss 형태의 문자열로 변환 (비교용)
    val currentTimeString = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.KOREAN).format(now.time)
    
    // 현재 시각 이후의 열차만 필터링 (자정 넘어서는 예외처리 필요 시 보완)
    val upcoming = schedules.filter { it.leftTime >= currentTimeString }.take(2)
    
    return upcoming.map { schedule ->
        val arrTime = try {
            sdf.parse(schedule.leftTime)
        } catch (e: Exception) {
            null
        }
        
        val msg = if (arrTime != null) {
            val arrCal = java.util.Calendar.getInstance()
            arrCal.time = arrTime
            arrCal.set(java.util.Calendar.YEAR, now.get(java.util.Calendar.YEAR))
            arrCal.set(java.util.Calendar.MONTH, now.get(java.util.Calendar.MONTH))
            arrCal.set(java.util.Calendar.DAY_OF_MONTH, now.get(java.util.Calendar.DAY_OF_MONTH))
            
            val diffMs = arrCal.timeInMillis - now.timeInMillis
            val diffMin = diffMs / (1000 * 60)
            if (diffMin <= 0) "곧 도착" else "${diffMin}분 후"
        } else {
            schedule.leftTime.substring(0, 5) // HH:mm
        }

        RealtimeArrival(
            subwayId = "",
            updnLine = "",
            trainLineName = "",
            statnFid = "",
            statnTid = "",
            ordkey = "",
            subwayList = "",
            btrainSttus = "",
            bstatnNm = schedule.destination.ifEmpty { "종착" },
            barvlDt = "",
            trainNumber = "",
            arrivalMessage1 = msg,
            arrivalMessage2 = "",
            lstcarAt = "0"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrivalCard(
    modifier: Modifier, 
    arrivals: List<RealtimeArrival>, 
    destination: String,
    fullSchedules: List<com.jonghyeok.ezegot.api.TimeTableSchedule> = emptyList()
) {
    var showSheet by remember { mutableStateOf(false) }
    
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            FullTimetableSheet(destination, fullSchedules) { showSheet = false }
        }
    }
    
    Surface(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        color = SurfaceWhite
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            // 도착 정보 및 버튼 외 다른 콘텐츠는 16dp 패딩 적용
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // 방면 제목 (중복 "방면" 제거 및 말줄임표 적용)
                val displayDestination = destination.replace("방면", "").trim()
                Text(
                    text = "$displayDestination 방면",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(12.dp))
                
                // 도착 리스트 (무조건 2개만 노출되도록 강제)
                val displayArrivals = arrivals.take(2)
                displayArrivals.forEach { arrival ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp), // 행 높이 명시적 고정
                        horizontalArrangement = Arrangement.Start, // 좌측 정렬로 변경하여 간격 축소
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = arrival.bstatnNm,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                if (arrival.ordkey.lastOrNull() == '1') {
                                    Spacer(Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = com.jonghyeok.ezegot.ui.theme.ArrivalRed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "급행",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = com.jonghyeok.ezegot.ui.theme.ArrivalRed,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.width(4.dp)) // 목적지와 시간 사이 간격 추가 축소 (4dp)
                        Text(
                            text = arrival.getFormattedMessage(),
                            style = MaterialTheme.typography.bodySmall,
                            color = ArrivalRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp)) // 행 간 간격 고정
                }
                
                // 높이 고정: 데이터가 1개이거나 없을 때 빈 자리 채우기
                val emptySlots = 2 - displayArrivals.size
                repeat(emptySlots) { 
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp), // 빈 행도 동일한 높이 고정
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (displayArrivals.isEmpty() && it == 0) "도착 정보 없음" else " ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHint
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            
            // 시간표 버튼: 카드 양 끝에서 8dp 간격 확보 (상시 노출)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showSheet = true },
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Navy900, Navy700)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "시간표 전체보기",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FullTimetableSheet(
    direction: String,
    schedules: List<com.jonghyeok.ezegot.api.TimeTableSchedule>,
    onClose: () -> Unit
) {
    val now = remember { java.util.Calendar.getInstance() }
    val nowHHmmss = remember {
        java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.KOREAN).format(now.time)
    }
    val currentHour = remember { now.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0') }
    val grouped = remember(schedules) { schedules.groupBy { it.leftTime.take(2) } }

    fun hourBandColor(hour: String): Color {
        val h = hour.toIntOrNull() ?: 12
        return when {
            h in 5..8   -> Color(0xFFE3F2FD)
            h in 9..11  -> Color(0xFFE8F5E9)
            h in 12..13 -> Color(0xFFFFFDE7)
            h in 14..17 -> Color(0xFFFFF3E0)
            h in 18..21 -> Color(0xFFEDE7F6)
            else         -> Color(0xFFECEFF1)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── 그라데이션 헤더 ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Navy900, Navy800)))
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SkyBlue400.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = direction,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBlue300,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "하루 시간표",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextOnDark
                    )
                }
            }
        }

        if (schedules.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚇", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(12.dp))
                    Text("시간표 정보가 없습니다", style = MaterialTheme.typography.bodyLarge, color = TextHint)
                }
            }
        } else {
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            val tabRowState = androidx.compose.foundation.lazy.rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            
            // 키(시간대)는 오름차순 정렬 유지 (05, 06, ..., 23)
            val keys = grouped.keys.sorted()
            val currentHourIndex = keys.indexOfFirst { it >= currentHour }

            // ── 시간대 카테고리 탭 ──
            androidx.compose.foundation.lazy.LazyRow(
                state = tabRowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(vertical = 12.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(keys.size) { index ->
                    val hour = keys[index]
                    val isCurrent = hour == currentHour
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isCurrent) Navy800 else BackgroundLight,
                        border = if (!isCurrent) androidx.compose.foundation.BorderStroke(1.dp, DividerColor) else null,
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                // 탭 클릭 → 리스트 해당 시간대로 스크롤
                                listState.animateScrollToItem(index * 2)
                                // 탭 자체도 선택된 탭이 보이도록 스크롤
                                tabRowState.animateScrollToItem(index)
                            }
                        }
                    ) {
                        Text(
                            text = "${hour}시",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) Color.White else TextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = DividerColor, thickness = 1.dp)

            // 처음 열릴 때, 현재 시간대로 탭 및 리스트 동시 스크롤
            LaunchedEffect(grouped) {
                if (currentHourIndex != -1) {
                    listState.scrollToItem(currentHourIndex * 2)
                    tabRowState.scrollToItem(currentHourIndex)
                }
            }


            androidx.compose.foundation.lazy.LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 48.dp)
            ) {
                keys.forEach { hour ->
                    val items = grouped[hour] ?: emptyList()
                    val isPastHour   = hour < currentHour
                    val isCurrentHour = hour == currentHour
                    val bandColor    = if (isPastHour) Color(0xFFF5F5F5) else hourBandColor(hour)

                    // 시간대 헤더 행
                    item(key = "h_$hour") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bandColor)
                                .padding(start = 24.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${hour}시",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isCurrentHour) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isPastHour) TextHint else Navy900
                                )
                                if (isCurrentHour) {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(shape = RoundedCornerShape(20.dp), color = ArrivalRed) {
                                        Text(
                                            "NOW",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 해당 시간대 열차 칩들
                    item(key = "i_$hour") {
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bandColor)
                                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items.forEach { schedule ->
                                val isPast = schedule.leftTime < nowHHmmss
                                val isNext = !isPast &&
                                    schedules.firstOrNull { it.leftTime >= nowHHmmss }?.leftTime == schedule.leftTime

                                val chipBg = when {
                                    isNext  -> Navy800
                                    isPast  -> Color(0xFFEEEEEE)
                                    else    -> SurfaceWhite
                                }
                                val timeColor = when {
                                    isNext -> SkyBlue300
                                    isPast -> TextHint
                                    else   -> TextPrimary
                                }
                                val destColor = when {
                                    isNext -> TextOnDark.copy(alpha = 0.75f)
                                    isPast -> TextHint.copy(alpha = 0.55f)
                                    else   -> TextSecondary
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = chipBg,
                                    shadowElevation = when {
                                        isNext -> 6.dp
                                        isPast -> 0.dp
                                        else   -> 2.dp
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${hour}:${schedule.leftTime.substring(3, 5)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = timeColor
                                        )
                                        if (schedule.destination.isNotEmpty() || schedule.isExpressTrain()) {
                                            Spacer(Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (schedule.isExpressTrain()) {
                                                    Text(
                                                        text = "급행",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                        color = ArrivalRed,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    )
                                                }
                                                Text(
                                                    text = schedule.destination,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = destColor,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun StationMapCard(stationLocation: StationInfoResponse) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        color = SurfaceWhite
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "역 위치",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(
                            LatLng(stationLocation.latitude, stationLocation.longitude), 15f
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun StationInfoCard(address: String, facility: com.jonghyeok.ezegot.api.FacilityInfoResponse? = null) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        color = SurfaceWhite
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "기기 정보 및 편의시설",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            InfoRow(label = "주소", value = address)
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "대표번호", value = "1544-7788")
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "유실물센터", value = "1544-7788")
            
            // 편의시설 데이터가 로드되었다면 표시
            facility?.facilities?.firstOrNull()?.let { fac ->
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (fac.hasElevator) FacilityBadge("엘리베이터")
                    if (fac.hasWheelchairLift) FacilityBadge("휠체어 리프트")
                    if (fac.restroomLocation.isNotEmpty()) FacilityBadge("화장실: ${fac.restroomLocation}")
                }
            }
        }
    }
}

@Composable
fun FacilityBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SkyBlue400.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Navy800,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextHint,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun StationTimeTableCard(
    up: com.jonghyeok.ezegot.api.TimeTableResponse?,
    down: com.jonghyeok.ezegot.api.TimeTableResponse?,
    upDtLabel: String,
    dnDtLabel: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        color = SurfaceWhite
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "첫차 / 막차 시간표",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            val upEmpty = up?.schedules?.isEmpty() ?: false
            val downEmpty = down?.schedules?.isEmpty() ?: false
            
            if (up != null && down != null && upEmpty && downEmpty) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "해당 노선(코레일 등)은 서울 공공데이터에서 시간표를 제공하지 않습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 상행
                    Column(modifier = Modifier.weight(1f)) {
                        val isLoading = up == null
                        val upEmpty = up?.schedules?.isEmpty() ?: false

                        // 로딩 중에는 "상행/내선" 등 기본 명칭 사용, 완료 후에는 대표 목적지 사용
                        val repUpDest = if (!isLoading && !upEmpty) {
                            up!!.schedules.mapNotNull { it.destination.ifEmpty { null } }
                                .groupBy { it }
                                .maxByOrNull { it.value.size }?.key ?: upDtLabel
                        } else {
                            // "왕십리 방면" -> "상행", "성수내선 방면" -> "내선"
                            if (upDtLabel.contains("내선")) "내선" else "상행"
                        }

                        Text(
                            text = repUpDest.replace("방면", "").trim() + " 방면",
                            style = MaterialTheme.typography.labelMedium,
                            color = Navy800,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        
                        if (isLoading) {
                            TimeTableRow("첫차", "00:00")
                            Spacer(Modifier.height(2.dp))
                            TimeTableRow("막차", "00:00")
                        } else if (!upEmpty) {
                            // 04:00 기준 정렬 (첫차/막차 판단 로직)
                            val sortedSchedules = up!!.schedules.sortedBy { 
                                val hour = it.leftTime.take(2).toIntOrNull() ?: 0
                                if (hour < 4) hour + 24 else hour
                            }
                            val first = sortedSchedules.firstOrNull()
                            val last = sortedSchedules.lastOrNull()
                            
                            first?.let { TimeTableRow("첫차", it.leftTime) }
                            Spacer(Modifier.height(2.dp))
                            last?.let { TimeTableRow("막차", it.leftTime) }
                        } else {
                            Text("정보 없음", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    
                    // 하행
                    Column(modifier = Modifier.weight(1f)) {
                        val isLoading = down == null
                        val downEmpty = down?.schedules?.isEmpty() ?: false

                        // 로딩 중에는 "하행/외선" 등 기본 명칭 사용, 완료 후에는 대표 목적지 사용
                        val repDnDest = if (!isLoading && !downEmpty) {
                            down!!.schedules.mapNotNull { it.destination.ifEmpty { null } }
                                .groupBy { it }
                                .maxByOrNull { it.value.size }?.key ?: dnDtLabel
                        } else {
                            // "인천 방면" -> "하행", "성수외선 방면" -> "외선"
                            if (dnDtLabel.contains("외선")) "외선" else "하행"
                        }

                        Text(
                            text = repDnDest.replace("방면", "").trim() + " 방면",
                            style = MaterialTheme.typography.labelMedium,
                            color = Navy800,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        
                        if (isLoading) {
                            TimeTableRow("첫차", "00:00")
                            Spacer(Modifier.height(2.dp))
                            TimeTableRow("막차", "00:00")
                        } else if (!downEmpty) {
                            // 04:00 기준 정렬 (첫차/막차 판단 로직)
                            val sortedSchedules = down!!.schedules.sortedBy { 
                                val hour = it.leftTime.take(2).toIntOrNull() ?: 0
                                if (hour < 4) hour + 24 else hour
                            }
                            val first = sortedSchedules.firstOrNull()
                            val last = sortedSchedules.lastOrNull()
 
                            first?.let { TimeTableRow("첫차", it.leftTime) }
                            Spacer(Modifier.height(2.dp))
                            last?.let { TimeTableRow("막차", it.leftTime) }
                        } else {
                            Text("정보 없음", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeTableRow(label: String, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Navy900.copy(alpha = 0.05f)
        ) {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = Navy800,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(text = time.take(5), style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

