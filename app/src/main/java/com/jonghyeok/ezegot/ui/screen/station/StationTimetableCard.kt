package com.jonghyeok.ezegot.ui.screen.station

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jonghyeok.ezegot.api.TimeTableResponse
import com.jonghyeok.ezegot.api.TimeTableSchedule
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.ui.theme.ArrivalRed
import com.jonghyeok.ezegot.ui.theme.Navy800
import com.jonghyeok.ezegot.ui.theme.Navy900
import com.jonghyeok.ezegot.ui.theme.SurfaceWhite
import com.jonghyeok.ezegot.ui.theme.TextPrimary
import com.jonghyeok.ezegot.ui.theme.TextSecondary

/**
 * 전체 시간표에서 현재 시점 이후로 가장 빨리 도착할 2대를 골라
 * [RealtimeArrival] 형태로 변환한다 (도착 카드가 두 모드를 같은 타입으로 다루기 위함).
 */
internal fun getUpcomingTrainsFromTimeTable(schedules: List<TimeTableSchedule>): List<RealtimeArrival> {
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

/**
 * 상·하행 방면 라벨을 만든다.
 *
 * 도착 정보 카드가 쓰는 라벨(실시간 `trainLineName`에서 뽑은 "교대 / 역삼")을
 * 그대로 쓰되, 두 값이 같거나 비어 있으면 방향 표기로 되돌린다.
 * 2호선은 상하행 대신 내선/외선이 오므로 그 표기를 따른다.
 */
private fun directionLabels(upDtLabel: String, dnDtLabel: String): Pair<String, String> {
    val up = upDtLabel.replace("방면", "").trim()
    val dn = dnDtLabel.replace("방면", "").trim()

    val isLoop = upDtLabel.contains("내선") || dnDtLabel.contains("외선")
    return if (up.isEmpty() || dn.isEmpty() || up == dn) {
        if (isLoop) "내선" to "외선" else "상행" to "하행"
    } else {
        up to dn
    }
}

/**
 * 첫차 / 막차 시간표 카드.
 *
 * 표시 우선순위:
 * 1. [errorMessage]가 있으면 실패 안내 (조회를 시도했으나 상·하행 모두 못 받은 경우)
 * 2. 응답은 왔는데 상·하행 모두 비어 있으면 미제공 노선 안내
 * 3. 그 외에는 방향별 첫차·막차. `up`/`down`이 null이면 아직 로딩 중이라 placeholder를 보여준다.
 */
@Composable
internal fun StationTimeTableCard(
    up: TimeTableResponse?,
    down: TimeTableResponse?,
    upDtLabel: String,
    dnDtLabel: String,
    errorMessage: String? = null
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

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArrivalRed,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "네트워크 상태를 확인한 뒤 다시 들어와 주세요.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else if (up != null && down != null && upEmpty && downEmpty) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "해당 노선(코레일 등)은 서울 공공데이터에서 시간표를 제공하지 않습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                // 방면 라벨은 위쪽 도착 정보 카드와 같은 값을 쓴다.
                // 시간표의 destination(종착역)으로 라벨을 만들면 2호선 같은 순환선에서
                // 상·하행 종착역이 모두 "성수"라 좌우 라벨이 똑같아진다.
                // 방면(교대 / 역삼)은 방향마다 다르므로 이쪽이 구분에 맞다.
                val (upLabel, dnLabel) = directionLabels(upDtLabel, dnDtLabel)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 상행
                    Column(modifier = Modifier.weight(1f)) {
                        val isLoading = up == null
                        val upEmpty = up?.schedules?.isEmpty() ?: false

                        Text(
                            text = "$upLabel 방면",
                            style = MaterialTheme.typography.labelMedium,
                            color = Navy800,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

                        Text(
                            text = "$dnLabel 방면",
                            style = MaterialTheme.typography.labelMedium,
                            color = Navy800,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
private fun TimeTableRow(label: String, time: String) {
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
