package com.jonghyeok.ezegot.ui.screen.station

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.api.TimeTableSchedule
import com.jonghyeok.ezegot.db.SubwayAlarmEntity
import com.jonghyeok.ezegot.dto.RealtimeArrival

/**
 * 한 방면(상행 또는 하행)의 도착 카드.
 *
 * 도착 2건을 표시하고, 열차별 알람 설정/해제와 전체 시간표 시트를 띄운다.
 * 데이터가 1건 이하일 때도 카드 높이가 흔들리지 않도록 빈 행으로 채운다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ArrivalCard(
    modifier: Modifier,
    arrivals: List<RealtimeArrival>,
    destination: String,
    fullSchedules: List<TimeTableSchedule> = emptyList(),
    activeAlarms: List<SubwayAlarmEntity>,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSetAlarm: (RealtimeArrival, Int) -> Unit,
    onCancelAlarm: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var selectedArrivalForAlarm by remember { mutableStateOf<RealtimeArrival?>(null) }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            FullTimetableSheet(destination, fullSchedules) { showSheet = false }
        }
    }

    if (selectedArrivalForAlarm != null) {
        AlertDialog(
            onDismissRequest = { selectedArrivalForAlarm = null },
            title = { Text("도착 알림 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = { Text("열차 도착 몇 분 전에 알림을 받을까요?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 5).forEach { min ->
                        Button(
                            onClick = {
                                selectedArrivalForAlarm?.let { onSetAlarm(it, min) }
                                selectedArrivalForAlarm = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("${min}분 전")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedArrivalForAlarm = null }) {
                    Text("취소", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Surface(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            // 도착 정보 및 버튼 외 다른 콘텐츠는 16dp 패딩 적용
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // 방면 제목 + 이 방향의 즐겨찾기 토글
                val displayDestination = destination.replace("방면", "").trim()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$displayDestination 방면",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // 즐겨찾기가 방향 단위라 별도 방면마다 하나씩 둔다
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                // 도착 리스트 (무조건 2개만 노출되도록 강제)
                val displayArrivals = arrivals.take(2)
                displayArrivals.forEach { arrival ->
                    val isAlarmSet = activeAlarms.any { it.trainNo == arrival.trainNumber }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = arrival.bstatnNm,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (arrival.ordkey.lastOrNull() == '1') {
                                    Spacer(Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "급행",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = arrival.getFormattedMessage(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (arrival.trainNumber.isNotEmpty()) {
                            // 터치 영역은 IconButton 기본값에 맡기고 아이콘만 16dp로 줄인다.
                            // 다만 이 Row가 height(32.dp)로 고정돼 있어 세로는 32dp로 잘린다.
                            IconButton(
                                onClick = {
                                    if (isAlarmSet) {
                                        onCancelAlarm(arrival.trainNumber)
                                    } else {
                                        selectedArrivalForAlarm = arrival
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isAlarmSet) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                                    // 켜짐/꺼짐이 아이콘 모양으로만 구분돼 있어 상태를 문구로 구분한다
                                    contentDescription = if (isAlarmSet) "알람 해제" else "알람 설정",
                                    tint = if (isAlarmSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // 높이 고정: 데이터가 1개이거나 없을 때 빈 자리 채우기
                val emptySlots = 2 - displayArrivals.size
                repeat(emptySlots) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (displayArrivals.isEmpty() && it == 0) "도착 정보 없음" else " ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // 시간표 진입점. 부차 기능이라 채움 버튼이 아닌 텍스트 링크로 둔다
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showSheet = true }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "시간표 전체보기",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
