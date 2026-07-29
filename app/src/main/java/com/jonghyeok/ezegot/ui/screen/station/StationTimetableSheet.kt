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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonghyeok.ezegot.api.TimeTableSchedule
import kotlinx.coroutines.delay
import java.time.Clock
import java.time.LocalTime

/**
 * 하루 전체 시간표 바텀시트.
 *
 * 화면에서 하려는 일은 둘뿐이다 — "지금 이후 다음 열차"와 "특정 시간대 열차".
 * 위아래로 나눠 각각에 답한다. 지난 열차는 지우지 않고 접어 둔다.
 *
 * 예전 구조는 시간대마다 배경색을 6종으로 칠하고 열차 하나를 96×72dp 카드로
 * 그려서, 하루 200편이 끝없이 이어지고 행선지가 카드마다 반복됐다.
 * 시간대 구간은 행선지별로 줄을 나눠 분만 나열한다. 줄 앞 라벨이 곧 범례라
 * 색이나 기호를 쓰지 않아도 구분되고, 세로 길이가 1/3로 줄어든다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StationTimetableSheet(
    stationName: String,
    upLabel: String,
    dnLabel: String,
    upSchedules: List<TimeTableSchedule>,
    dnSchedules: List<TimeTableSchedule>,
    startWithUp: Boolean,
    onClose: () -> Unit,
    clock: Clock = Clock.systemDefaultZone()
) {
    var showUp by remember { mutableStateOf(startWithUp) }
    var pastExpanded by remember { mutableStateOf(false) }

    // 시트를 오래 열어두면 "지금"과 "다음 열차"가 실제와 어긋난다.
    // 분이 바뀔 때만 의미가 있으므로 20초 간격이면 충분하다.
    var nowMinutes by remember { mutableIntStateOf(nowServiceMinutes(clock)) }
    LaunchedEffect(clock) {
        while (true) {
            delay(20_000)
            nowMinutes = nowServiceMinutes(clock)
        }
    }

    val schedules = if (showUp) upSchedules else dnSchedules
    val sorted = remember(schedules) { schedules.sortedBy { it.leftTime.toServiceMinutes() } }

    val (past, upcoming) = remember(sorted, nowMinutes) {
        sorted.partition { it.leftTime.toServiceMinutes() < nowMinutes }
    }
    val nextTrains = upcoming.take(NEXT_TRAIN_COUNT)

    // 시트 안 목록에 높이를 정해 줘야 LazyColumn이 스크롤된다.
    // 화면 비율로 잡아 기기가 달라져도 시트가 화면을 덮거나 반쪽만 차지하지 않게 한다.
    val listMaxHeight = (LocalConfiguration.current.screenHeightDp * 0.70f).dp

    // 제스처/버튼 내비게이션 여백. 없으면 마지막 시간대가 시스템 바에 가린다.
    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {

        // ── 제목 ────────────────────────────────────────────────
        // 예전에는 방향이 제목 옆 칩이라 역명처럼 읽혔다. 제목은 역명으로 두고
        // 방향은 아래 전환 탭으로 완전히 분리한다.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stationName}역 시간표",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── 방향 전환 ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DirectionTab(
                label = "$upLabel 방면",
                selected = showUp,
                modifier = Modifier.weight(1f)
            ) { showUp = true }
            DirectionTab(
                label = "$dnLabel 방면",
                selected = !showUp,
                modifier = Modifier.weight(1f)
            ) { showUp = false }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier.heightIn(max = listMaxHeight),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── 지금 + 다음 열차 ────────────────────────────────
            item(key = "next") {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text(
                        text = "지금 ${nowMinutes.toClockLabel()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(10.dp))

                    if (nextTrains.isEmpty()) {
                        Text(
                            text = "오늘 남은 열차가 없습니다",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        nextTrains.forEachIndexed { index, schedule ->
                            NextTrainRow(
                                schedule = schedule,
                                minutesAway = schedule.leftTime.toServiceMinutes() - nowMinutes,
                                isNext = index == 0
                            )
                        }
                    }
                }
            }

            // ── 지난 열차 (접힘) ────────────────────────────────
            if (past.isNotEmpty()) {
                item(key = "past-toggle") {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pastExpanded = !pastExpanded }
                            .padding(horizontal = 20.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (pastExpanded) Icons.Default.KeyboardArrowDown
                            else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "지난 열차 (${past.size}편)",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (pastExpanded) {
                    hourSections(past, dimmed = true)
                }
            }

            // ── 앞으로 남은 시간대 ──────────────────────────────
            if (upcoming.isNotEmpty()) {
                item(key = "future-divider") {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
                hourSections(upcoming, dimmed = false)
            }
        }
    }
}

// ── 시간대 구간 ─────────────────────────────────────────────────
/**
 * 시간대별 섹션을 LazyColumn에 펼친다.
 *
 * 시간대 하나가 아이템 하나다. 예전에는 헤더와 칩 행을 각각 아이템으로 넣고
 * `animateScrollToItem(index * 2)`로 위치를 계산해서, 구조를 조금만 바꿔도
 * 스크롤이 조용히 어긋났다. 이제 인덱스를 계산하는 곳이 없다.
 */
@OptIn(ExperimentalLayoutApi::class)
private fun androidx.compose.foundation.lazy.LazyListScope.hourSections(
    schedules: List<TimeTableSchedule>,
    dimmed: Boolean
) {
    val byHour = schedules.groupBy { it.leftTime.take(2) }.toSortedMap()

    byHour.forEach { (hour, items) ->
        item(key = "${if (dimmed) "p" else "f"}-$hour") {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(
                    text = "${hour.trimStart('0').ifEmpty { "0" }}시",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Medium,
                    color = if (dimmed) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))

                // 행선지별로 줄을 나눈다. 줄 앞 라벨이 범례 역할을 하므로
                // 색이나 기호 없이 위치만으로 구분된다.
                // 편수가 많은 행선지가 위로 온다.
                items.groupBy { it.destination }
                    .entries
                    .sortedByDescending { it.value.size }
                    .forEach { (destination, trains) ->
                        DestinationMinuteRow(
                            destination = destination,
                            trains = trains,
                            dimmed = dimmed
                        )
                    }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DestinationMinuteRow(
    destination: String,
    trains: List<TimeTableSchedule>,
    dimmed: Boolean
) {
    val textColor = if (dimmed) MaterialTheme.colorScheme.tertiary
    else MaterialTheme.colorScheme.onSurface
    val labelColor = if (dimmed) MaterialTheme.colorScheme.tertiary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = destination,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = labelColor,
            maxLines = 1,
            modifier = Modifier
                .width(DESTINATION_LABEL_WIDTH)
                .padding(top = 2.dp)
        )
        // 한 줄 12편을 넘기면 접는다. 실측상 시간대 최다 16편이 행선지 2종으로
        // 갈리므로 줄당 8~10편이라 거의 발생하지 않는다.
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            trains.forEach { train ->
                MinuteCell(
                    minute = train.leftTime.substring(3, 5),
                    express = train.isExpressTrain(),
                    color = textColor
                )
            }
        }
    }
}

/**
 * 분 하나. 2자리 고정폭이라 세로로 자릿수가 흔들리지 않는다.
 * 급행은 색만으로 구분하면 색각 이상에서 구분되지 않으므로 "급" 글자를 함께 붙인다.
 */
@Composable
private fun MinuteCell(minute: String, express: Boolean, color: Color) {
    Row(
        modifier = Modifier.widthIn(min = MINUTE_CELL_WIDTH),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = minute,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                letterSpacing = 0.sp,
                fontFeatureSettings = "tnum"
            ),
            color = color,
            textAlign = TextAlign.Center
        )
        if (express) {
            Text(
                text = "급",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 1.dp)
            )
        }
    }
}

// ── 다음 열차 ───────────────────────────────────────────────────
@Composable
private fun NextTrainRow(
    schedule: TimeTableSchedule,
    minutesAway: Int,
    isNext: Boolean
) {
    val emphasisColor =
        if (isNext) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Column {
        if (isNext) {
            Text(
                text = "다음 열차",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = schedule.leftTime.take(5),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontFeatureSettings = "tnum"
                ),
                fontWeight = FontWeight.Medium,
                color = emphasisColor
            )
            if (schedule.isExpressTrain()) {
                Spacer(Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "급행",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "${schedule.destination}행",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (minutesAway <= 0) "곧 도착" else "${minutesAway}분 후",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontFeatureSettings = "tnum"
                ),
                fontWeight = FontWeight.Medium,
                color = emphasisColor
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun DirectionTab(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                fontWeight = FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ── 시각 계산 ───────────────────────────────────────────────────
/**
 * 운행일 기준 분(minute).
 *
 * 시간표는 자정을 넘겨도 "24:49"처럼 이어 쓴다. 벽시계 00:49를 그대로 비교하면
 * 그 열차가 하루의 맨 끝으로 밀려 "이미 지났다"고 판정된다.
 * 04시 이전은 전날 운행의 연장으로 보고 24를 더한다.
 */
private fun String.toServiceMinutes(): Int {
    val hour = substring(0, 2).toIntOrNull() ?: return 0
    val minute = substring(3, 5).toIntOrNull() ?: return 0
    return (if (hour < SERVICE_DAY_START_HOUR) hour + 24 else hour) * 60 + minute
}

private fun nowServiceMinutes(clock: Clock): Int {
    val now = LocalTime.now(clock)
    val hour = if (now.hour < SERVICE_DAY_START_HOUR) now.hour + 24 else now.hour
    return hour * 60 + now.minute
}

/** 운행일 분을 다시 "18:05" 표기로. 24시를 넘겨도 시간표 표기와 맞춘다 */
private fun Int.toClockLabel(): String = "%02d:%02d".format(this / 60, this % 60)

/** 04시 이전은 전날 운행의 연장으로 본다 */
private const val SERVICE_DAY_START_HOUR = 4

private const val NEXT_TRAIN_COUNT = 6
private val DESTINATION_LABEL_WIDTH = 44.dp
private val MINUTE_CELL_WIDTH = 24.dp
