package com.jonghyeok.ezegot.ui.screen.station

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.util.ArrivalEmphasis
import com.jonghyeok.ezegot.util.emphasis

/**
 * 한 방향의 도착 카드.
 *
 * 예전에는 좌우 2열로 붙여 폭이 154dp밖에 안 됐고, 데이터가 1건이어도
 * 2줄 높이를 강제로 채워 카드 아래가 비었다. 방향마다 카드를 하나씩 두고
 * 열차 수만큼만 행을 그린다.
 */
@Composable
internal fun ArrivalDirectionCard(
    directionLabel: String,
    lineColor: Color,
    arrivals: List<RealtimeArrival>,
    isAlarmOn: Boolean,
    onAlarmClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        // 카드 헤더.
        // 높이를 38dp로 고정한다. IconButton 기본 크기가 48dp라 그대로 두면
        // 행이 64dp까지 늘어나 제목 아래에 빈 공간이 생겼다.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .height(38.dp)
                .padding(start = 13.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(lineColor)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "$directionLabel 방면",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            // 헤더가 38dp라 IconButton 기본 48dp를 그대로 쓸 수 없다.
            // 터치 영역은 헤더 높이만큼(38dp) 확보한다.
            IconButton(onClick = onAlarmClick, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (isAlarmOn) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                    contentDescription = if (isAlarmOn) "알림 해제" else "알림 예약",
                    tint = if (isAlarmOn) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        if (arrivals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "도착 정보 없음",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        } else {
            arrivals.take(3).forEachIndexed { index, arrival ->
                if (index > 0) {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
                ArrivalTrainRow(arrival = arrival, isFirst = index == 0)
            }
        }
    }
}

@Composable
private fun ArrivalTrainRow(arrival: RealtimeArrival, isFirst: Boolean) {
    val emphasis = arrival.emphasis()
    val color = when (emphasis) {
        ArrivalEmphasis.URGENT -> MaterialTheme.colorScheme.error
        ArrivalEmphasis.NORMAL -> MaterialTheme.colorScheme.onSurface
        ArrivalEmphasis.DISTANT -> MaterialTheme.colorScheme.onSurfaceVariant
        ArrivalEmphasis.INACTIVE -> MaterialTheme.colorScheme.tertiary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 시간 열은 폭을 고정해 여러 행의 오른쪽 텍스트가 세로로 정렬되게 한다
        Box(modifier = Modifier.width(46.dp)) {
            ArrivalTimeText(
                message = arrival.getFormattedMessage(),
                color = color,
                fontSize = if (isFirst) 21.sp else 15.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = arrival.rowDetail(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (emphasis == ArrivalEmphasis.URGENT) {
            Spacer(Modifier.width(6.dp))
            ImminentChip()
        }
    }
}

/** "성수행 · 역삼 출발". 뒷부분은 arvlMsg3(전역 상태)로, 그동안 표시하지 않던 값이다 */
private fun RealtimeArrival.rowDetail(): String {
    val destination = bstatnNm.trim().takeIf { it.isNotEmpty() }?.let { "${it}행" }
    val progress = arrivalMessage2.trim().takeIf { it.isNotEmpty() }
    return listOfNotNull(destination, progress).joinToString(" · ")
}

@Composable
private fun ImminentChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "곧 도착",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            maxLines = 1
        )
    }
}

/**
 * 도착 시간. 숫자와 "분"을 분리해 단위를 작게 쓰고,
 * 숫자에 tabular figures를 줘 자릿수가 달라도 열이 흔들리지 않게 한다.
 */
@Composable
internal fun ArrivalTimeText(
    message: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    val minutes = Regex("(\\d+)분").find(message)?.groupValues?.get(1)
    Row(verticalAlignment = Alignment.Bottom) {
        if (minutes != null) {
            Text(
                text = minutes,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = fontSize,
                    letterSpacing = (-0.8).sp,
                    fontFeatureSettings = "tnum"
                ),
                color = color,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "분",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = color,
                modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
            )
        } else {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = color,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
