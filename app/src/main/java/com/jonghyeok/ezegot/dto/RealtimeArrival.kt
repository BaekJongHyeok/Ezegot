package com.jonghyeok.ezegot.dto

import com.jonghyeok.ezegot.util.ArrivalEstimator
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

// API 호출을 통해 실시간 도착 정보를 받아올 때 사용되는 DTO
@Root(name = "row", strict = false)
data class RealtimeArrival(
    // 지하철 호선
    @field:Element(name = "subwayId", required = false)
    var subwayId: String = "",

    // 상하행선구분 (0 : 상행/내선, 1 : 하행/외선)
    @field:Element(name = "updnLine", required = false)
    var updnLine: String = "",

    // 도착지방면 (성수행(목적지역) - 구로디지털단지방면(다음역))
    @field:Element(name = "trainLineNm", required = false)
    var trainLineName: String = "",

    // 이전 지하철역 ID
    @field:Element(name = "statnFid", required = false)
    var statnFid: String = "",

    // 다음 지하철역 ID
    @field:Element(name = "statnTid", required = false)
    var statnTid: String = "",

    // 도착예정열차순번 (상하행코드(1자리), 순번(첫번째, 두번째 열차 , 1자리), 첫번째 도착예정 정류장 - 현재 정류장(3자리), 목적지 정류장, 급행여부(1자리))
    @field:Element(name = "ordkey", required = false)
    var ordkey: String = "",

    // 연계호선ID
    @field:Element(name = "subwayList", required = false)
    var subwayList: String = "",

    // 열차 종류
    @field:Element(name = "btrainSttus", required = false)
    var btrainSttus: String = "",

    // 종착 지하철 명
    @field:Element(name = "bstatnNm", required = false)
    var bstatnNm: String = "",

    // 열차 도착 예정시간
    @field:Element(name = "barvlDt", required = false)
    var barvlDt: String = "",

    // 열차번호
    @field:Element(name = "btrainNo", required = false)
    var trainNumber: String = "",

    // 첫번째 도착 메세지 (도착, 출발, 진입 등)
    @field:Element(name = "arvlMsg2", required = false)
    var arrivalMessage1: String = "",

    // 두번째 도착 메세지 (종합운동장 도착, 12분후 (광명사거리) 등)
    @field:Element(name = "arvlMsg3", required = false)
    var arrivalMessage2: String = "",

    // 막차여부
    @field:Element(name = "lstcarAt", required = false)
    var lstcarAt: String = "",

    // 이 행이 만들어진 시각 ("2026-07-29 18:53:21").
    // 급행 계통이 일반보다 5분 가까이 뒤처져 오므로, 떠난 열차를 거르는 데 쓴다.
    @field:Element(name = "recptnDt", required = false)
    var receptionTime: String = "",
) {
    /**
     * 사용자 친화적인 실시간 도착 메시지 반환 함수.
     * 1순위: barvlDt (남은 시간 초) 정보를 활용하여 "N분 후" / "곧 도착"으로 변환
     * 2순위: barvlDt가 유효하지 않을 경우, arrivalMessage1에서 "[N]번째 전역"을 추출해 ArrivalEstimator로 정밀 시간(분) 추정.
     */
    fun getFormattedMessage(): String {
        // 1. barvlDt (남은 시간 초) 기반 처리
        val secondsLeft = barvlDt.toIntOrNull() ?: 0
        if (secondsLeft > 0) {
            val minutesLeft = secondsLeft / 60
            return if (minutesLeft > 0) {
                "${minutesLeft}분 후"
            } else {
                "곧 도착"
            }
        }

        // 2. barvlDt가 없으면 arvlMsg2를 정제해 쓴다.
        //
        // 꼬리에 붙는 "(다음 역)"만 뗀다. substringBefore("(")를 쓰면
        // "총신대입구(이수) 도착"처럼 역명에 괄호가 있는 역에서 역명이 잘려
        // "총신대입구"만 남고, 어느 패턴에도 걸리지 않아 역명이 그대로 화면에 나왔다.
        // greedy .* 라서 "6분 후 (상도(중앙대앞))" 같은 중첩 괄호도 통째로 걷힌다.
        val rawMessage = arrivalMessage1.replace(TRAILING_PARENTHESES, "").trim()
        
        // "[N]번째 전역" 패턴(예: "[3]번째 전역") 처리 -> 정밀 시간 계산(ArrivalEstimator)
        val stationRegex = Regex("\\[(\\d+)]번째 전역")
        val matchResult = stationRegex.find(rawMessage)
        
        if (matchResult != null) {
            val count = matchResult.groupValues[1].toIntOrNull() ?: return rawMessage
            val estimatedMinutes = ArrivalEstimator.estimateMinutesFromStations(this, count)
            return "${estimatedMinutes}분 후"
        }

        // 3. "전역"으로 시작하면 한 정거장 전이다 ("전역 도착", "전역 진입", "전역").
        //
        // 반드시 아래 endsWith 검사보다 먼저 와야 한다. "전역 도착"은 이전 역에
        // 도착했다는 뜻인데 endsWith("도착")에 먼저 걸리면 현재 역 도착과 구분되지
        // 않는다. 그러면 한 정거장(약 3분) 떨어진 열차를 "지금 들어온다"고 알리게 되고,
        // 인접한 두 역에서 같은 열차의 표시가 뒤집혀 보인다.
        // 수원·매교·수원시청 동시 수집 62건 중 12건(19%)이 이 경우였다.
        if (rawMessage.startsWith("전역")) {
            val estimatedMinutes = ArrivalEstimator.estimateMinutesFromStations(this, 1)
            return "${estimatedMinutes}분 후"
        }

        // 4. "XX역 도착", "XX도착" 등의 텍스트 처리 -> "도착"으로 통일
        if (rawMessage.endsWith("도착")) {
            return "도착"
        }
        
        // 5. "XX 진입", "XX진입" 등의 텍스트 처리 -> "진입"으로 통일
        if (rawMessage.endsWith("진입")) {
            return "진입"
        }
        
        // 6. "XX 출발", "XX출발" 등의 텍스트 처리 -> "출발"으로 통일
        if (rawMessage.endsWith("출발")) {
            return "출발"
        }

        // 일반 텍스트의 경우 불필요한 단어 제거 혹은 그대로 반환
        return rawMessage.ifEmpty { "정보 없음" }
    }

    /**
     * 남은 도착 시간(초). 알림 예약이 언제 깨어날지 계산하는 데 쓴다.
     *
     * [barvlDt]가 있으면 그 값이 실측이라 그대로 쓰고, 없으면 화면에 찍히는
     * "N분 후"를 초로 되돌린다. 즉 화면 표시와 항상 같은 값이다.
     *
     * 추정으로 나온 값은 분 단위 반올림을 거친 뒤라 초 정밀도가 없다.
     * 그 여부는 [hasMeasuredArrivalTime]으로 구분한다.
     */
    fun secondsUntilArrival(): Int {
        val measured = barvlDt.toIntOrNull() ?: 0
        if (measured > 0) return measured

        val minutes = Regex("(\\d+)분 후").find(getFormattedMessage())
            ?.groupValues?.get(1)?.toIntOrNull() ?: 0
        return minutes * 60
    }

    /** [secondsUntilArrival]이 실측인가 추정인가. 알림 예약의 여유 폭을 정할 때 쓴다 */
    fun hasMeasuredArrivalTime(): Boolean = (barvlDt.toIntOrNull() ?: 0) > 0

    private companion object {
        /** 문자열 끝에 붙는 "(다음 역)". 중첩 괄호까지 한 번에 걷도록 greedy를 쓴다 */
        val TRAILING_PARENTHESES = Regex("""\s*\(.*\)\s*$""")
    }
}
