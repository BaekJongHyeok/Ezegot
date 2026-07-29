package com.jonghyeok.ezegot.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.db.SubwayAlarmEntity
import com.jonghyeok.ezegot.ui.theme.getSubwayLineColor
import com.jonghyeok.ezegot.ui.theme.subwayLineOnTint
import com.jonghyeok.ezegot.ui.theme.subwayLineTint
import com.jonghyeok.ezegot.viewModel.AlarmViewModel

/**
 * 알림 탭. 예약된 도착 알람을 보여주고 지운다.
 *
 * 알람 생성은 역 상세에서만 한다. 여기서 새로 만들지 않는다.
 */
@Composable
fun AlarmScreen(viewModel: AlarmViewModel = hiltViewModel()) {
    val alarms by viewModel.alarms.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(title = "알림")

        if (alarms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "예약된 알림이 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "역 상세에서 열차별로 알림을 예약할 수 있어요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmRow(alarm = alarm, onCancel = { viewModel.cancel(alarm) })
                }
            }
        }
    }
}

@Composable
private fun AlarmRow(alarm: SubwayAlarmEntity, onCancel: () -> Unit) {
    val lineName = SubwayLine.getLineName(alarm.lineNumber) ?: alarm.lineNumber
    val lineColor = getSubwayLineColor(lineName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LineSquareBadge(lineName = lineName, lineColor = lineColor)
            Spacer(Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${alarm.stationName}역",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${alarm.destination}행 · 도착 ${alarm.alarmTimeThreshold / 60}분 전 알림",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "알림 취소",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/** 30dp 정사각 라운드 노선 뱃지. 연한 노선색 배경 + 진한 노선색 텍스트 */
@Composable
internal fun LineSquareBadge(
    lineName: String,
    lineColor: androidx.compose.ui.graphics.Color,
    size: androidx.compose.ui.unit.Dp = 30.dp
) {
    val tint = subwayLineTint(lineColor)
    // 연한 배경은 흰 카드 위에 얹히므로, 대비 계산의 기준도 카드 색이다
    val onTint = subwayLineOnTint(lineColor, MaterialTheme.colorScheme.surface)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(9.dp))
            .background(tint),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = lineName.toBadgeLabel(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = onTint,
            fontWeight = FontWeight.Medium
        )
    }
}

/** "2호선" → "2", "수인분당선" → "수인", "GTX-A" → "A" */
internal fun String.toBadgeLabel(): String {
    val digits = filter { it.isDigit() }.trimStart('0')
    if (digits.isNotEmpty()) return digits
    if (startsWith("GTX")) return substringAfter("-").ifEmpty { "GTX" }
    return removeSuffix("선").take(2)
}

/** 탭 화면 공통 헤더. 흰 배경 48dp */
@Composable
internal fun ScreenHeader(title: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
    }
}
