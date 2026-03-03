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

        // 2. barvlDt 정보가 없을 경우 기존 arvlMsg2(arrivalMessage1) 기반 문자열 정제 및 시간 추정
        val rawMessage = arrivalMessage1.substringBefore("(").trim()
        
        // "[N]번째 전역" 패턴(예: "[3]번째 전역") 처리 -> 정밀 시간 계산(ArrivalEstimator)
        val stationRegex = Regex("\\[(\\d+)]번째 전역")
        val matchResult = stationRegex.find(rawMessage)
        
        if (matchResult != null) {
            val count = matchResult.groupValues[1].toIntOrNull() ?: return rawMessage
            val estimatedMinutes = ArrivalEstimator.estimateMinutesFromStations(this, count)
            return "${estimatedMinutes}분 후"
        }

        // 3. "전역" 이라는 텍스트가 단독으로 올 경우 (1정거장 전)
        if (rawMessage == "전역") {
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
}
