package com.jonghyeok.ezegot.ui.screen.station

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.api.TimeTableResponse

/**
 * 첫차·막차 메트릭.
 *
 * 방향이 둘이라 방향별로 묶고, 각 묶음에 첫차·막차 카드를 가로 2열로 둔다.
 * 2×2 격자로 붙이면 어느 칸이 어느 방향인지 라벨 없이는 읽히지 않는다.
 *
 * 전체 시간표 진입점은 하나만 둔다. 예전에는 방향별 채움 버튼 2개 + 실시간/시간표
 * 토글까지 진입점이 셋이었다.
 */
@Composable
internal fun StationFirstLastSection(
    upLabel: String,
    dnLabel: String,
    up: TimeTableResponse?,
    down: TimeTableResponse?,
    errorMessage: String?,
    onOpenFullTimetable: () -> Unit
) {
    val upEmpty = up?.schedules?.isEmpty() ?: false
    val downEmpty = down?.schedules?.isEmpty() ?: false

    when {
        errorMessage != null -> NoticeCard(errorMessage, "네트워크 상태를 확인한 뒤 다시 들어와 주세요")
        up != null && down != null && upEmpty && downEmpty ->
            NoticeCard("시간표 미제공 노선", "코레일 등은 서울 공공데이터에서 시간표를 주지 않습니다")
        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DirectionMetricGroup(label = upLabel, response = up)
                DirectionMetricGroup(label = dnLabel, response = down)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenFullTimetable() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "전체 시간표",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectionMetricGroup(label: String, response: TimeTableResponse?) {
    // 04:00 기준으로 정렬해야 24시 넘는 막차가 뒤로 간다
    val sorted = response?.schedules?.sortedBy {
        val hour = it.leftTime.take(2).toIntOrNull() ?: 0
        if (hour < 4) hour + 24 else hour
    }
    val first = sorted?.firstOrNull()?.leftTime?.take(5)
    val last = sorted?.lastOrNull()?.leftTime?.take(5)

    Column {
        Text(
            text = "$label 방면",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(label = "첫차", value = first, modifier = Modifier.weight(1f))
            MetricCard(label = "막차", value = last, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value ?: "—",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontFeatureSettings = "tnum"
            ),
            color = if (value != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NoticeCard(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(13.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
