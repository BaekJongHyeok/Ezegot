package com.jonghyeok.ezegot.ui.screen.station

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jonghyeok.ezegot.SubwayLine
import com.jonghyeok.ezegot.api.TimeTableResponse
import com.jonghyeok.ezegot.db.SubwayAlarmEntity
import com.jonghyeok.ezegot.dto.RealtimeArrival
import com.jonghyeok.ezegot.dto.directionPairFor

/**
 * 열차 도착 정보 섹션.
 *
 * 실시간 / 시간표 스위치에 따라 상·하행 [ArrivalCard]에 넘길 목록을 만든다.
 * 2호선은 상하행 대신 "내선" / "외선"이 오므로 두 표기를 모두 필터에 넣는다.
 */
@Composable
internal fun ArrivalInfoSection(
    arrivals: List<RealtimeArrival>,
    line: String,
    upDt: String,
    dnDt: String,
    timeTable: Pair<TimeTableResponse?, TimeTableResponse?>?,
    activeAlarms: List<SubwayAlarmEntity>,
    favoriteDirections: Set<String>,
    onToggleFavoriteDirection: (String) -> Unit,
    onSetAlarm: (RealtimeArrival, Int) -> Unit,
    onCancelAlarm: (String) -> Unit
) {
    // 이 노선이 쓰는 방향 표기 (2호선은 내선/외선)
    val (upDirection, dnDirection) = directionPairFor(line)
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
            // 아래 도착 카드(16dp)와 좌측 정렬을 맞춘다
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "열차 도착 정보",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "실시간",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isRealtime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiary,
                    fontWeight = if (isRealtime) FontWeight.Bold else FontWeight.Normal
                )
                Switch(
                    checked = !isRealtime,
                    onCheckedChange = { isRealtime = !it },
                    modifier = Modifier.padding(horizontal = 6.dp).scale(0.75f),
                    colors = SwitchDefaults.colors(
                        // 썸은 표면이라 onPrimary(흰색)를 쓴다
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        // 꺼짐 트랙은 중립 회색이어야 켜짐과 구분된다
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = "시간표",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (!isRealtime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiary,
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
            ArrivalCard(
                modifier = Modifier.weight(1f),
                arrivals = upList,
                destination = upDt,
                fullSchedules = upFullSchedules,
                activeAlarms = activeAlarms,
                isFavorite = upDirection in favoriteDirections,
                onToggleFavorite = { onToggleFavoriteDirection(upDirection) },
                onSetAlarm = onSetAlarm,
                onCancelAlarm = onCancelAlarm
            )
            ArrivalCard(
                modifier = Modifier.weight(1f),
                arrivals = dnList,
                destination = dnDt,
                fullSchedules = dnFullSchedules,
                activeAlarms = activeAlarms,
                isFavorite = dnDirection in favoriteDirections,
                onToggleFavorite = { onToggleFavoriteDirection(dnDirection) },
                onSetAlarm = onSetAlarm,
                onCancelAlarm = onCancelAlarm
            )
        }
    }
}
