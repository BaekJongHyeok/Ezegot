package com.jonghyeok.ezegot.viewModel

import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.api.TimeTableResponse
import com.jonghyeok.ezegot.db.SubwayAlarmEntity
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival

/**
 * 역 상세 화면의 전체 상태.
 *
 * 로딩·성공·에러를 하나의 값으로 표현한다. 화면은 이 객체 하나만 구독한다.
 */
data class StationUiState(
    /** 실시간 도착 정보를 조회하는 중인지 */
    val isLoading: Boolean = false,

    val stationInfo: BasicStationInfo? = null,
    val arrivals: List<RealtimeArrival> = emptyList(),

    /**
     * 첫차·막차 시간표 (상행, 하행).
     *
     * `null`이면 아직 조회 전이라 화면은 로딩 placeholder를 보여준다.
     * 조회가 끝났는데 상·하행이 모두 `null`이면 실패로 보고 [errorMessage]를 채운다.
     */
    val timetable: Pair<TimeTableResponse?, TimeTableResponse?>? = null,

    val stationLocation: StationInfoResponse? = null,

    /**
     * 이 역에서 즐겨찾기에 담아둔 방향들("상행"/"하행"/"내선"/"외선").
     *
     * 즐겨찾기 단위가 역이 아니라 방향이라 Boolean 하나로는 표현할 수 없다.
     */
    val favoriteDirections: Set<String> = emptySet(),
    val isNotification: Boolean = false,
    val activeAlarms: List<SubwayAlarmEntity> = emptyList(),

    /**
     * 사용자에게 보여줄 실패 메시지. 현재는 시간표 조회 실패만 담는다.
     * 시간표 카드 안에 인라인으로 표시된다.
     */
    val errorMessage: String? = null
)
