package com.jonghyeok.ezegot.ui.screen.station

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.api.TimeTableSchedule
import com.jonghyeok.ezegot.ui.theme.ArrivalRed
import com.jonghyeok.ezegot.ui.theme.BackgroundLight
import com.jonghyeok.ezegot.ui.theme.DividerColor
import com.jonghyeok.ezegot.ui.theme.Navy800
import com.jonghyeok.ezegot.ui.theme.Navy900
import com.jonghyeok.ezegot.ui.theme.SkyBlue300
import com.jonghyeok.ezegot.ui.theme.SkyBlue400
import com.jonghyeok.ezegot.ui.theme.SurfaceWhite
import com.jonghyeok.ezegot.ui.theme.TextHint
import com.jonghyeok.ezegot.ui.theme.TextOnDark
import com.jonghyeok.ezegot.ui.theme.TextPrimary
import com.jonghyeok.ezegot.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * 하루 전체 시간표 바텀시트.
 *
 * 시간대별로 묶어 표시하고, 열릴 때 현재 시간대로 탭과 리스트를 함께 스크롤한다.
 * 리스트는 시간대마다 헤더 + 칩행 2개 아이템으로 구성되므로 스크롤 인덱스는 `index * 2`다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun FullTimetableSheet(
    direction: String,
    schedules: List<TimeTableSchedule>,
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
            val listState = rememberLazyListState()
            val tabRowState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            // 키(시간대)는 오름차순 정렬 유지 (05, 06, ..., 23)
            val keys = grouped.keys.sorted()
            val currentHourIndex = keys.indexOfFirst { it >= currentHour }

            // ── 시간대 카테고리 탭 ──
            LazyRow(
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
                        border = if (!isCurrent) BorderStroke(1.dp, DividerColor) else null,
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
                            color = if (isCurrent) TextOnDark else TextPrimary,
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


            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 48.dp)
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
                                            color = TextOnDark,
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
                        FlowRow(
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
                                    // alpha 0.55를 얹으면 실효 명암비가 2:1 아래로 떨어져 읽을 수 없다.
                                    // 지난 열차라는 것은 칩 배경색만으로 충분히 구분된다.
                                    isPast -> TextHint
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
                                                        style = MaterialTheme.typography.labelSmall,
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
