package com.jonghyeok.ezegot.api

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * 빠른 환승 정보 (공공데이터포털) 응답 예시
 */
data class TransferInfoResponse(
    val transferInfos: List<TransferInfo> = emptyList()
)

data class TransferInfo(
    val stationName: String,
    val targetLine: String,
    val fastTrainDoor: String // 예: "3-1"
)

/**
 * 역 편의시설 정보 응답 예시
 */
data class FacilityInfoResponse(
    val facilities: List<FacilityInfo> = emptyList()
)

data class FacilityInfo(
    val hasElevator: Boolean,
    val hasWheelchairLift: Boolean,
    val restroomLocation: String // 예: "게이트 안"
)

/**
 * 첫차 막차 시간표 응답 예시 (서울 열린데이터 광장)
 */
@Root(name = "SearchSTNTimeTableByFRCodeService", strict = false)
data class TimeTableResponse(
    @field:ElementList(name = "row", inline = true, required = false)
    var schedules: MutableList<TimeTableSchedule> = mutableListOf()
)

@Root(name = "row", strict = false)
data class TimeTableSchedule(
    @field:Element(name = "STATION_NM", required = false)
    var stationName: String = "",
    
    @field:Element(name = "LEFTTIME", required = false)
    var leftTime: String = "", // 예: "23:55:00"
    
    @field:Element(name = "SUBWAYSNAME", required = false)
    var destination: String = "", // 예: "의정부"
    
    @field:Element(name = "EXPRESS_YN", required = false)
    var express: String = "" // "G", "Y" 등 (급행 여부), "N" = 일반
) {
    // 서울 API: EXPRESS_YN = "G"(일반), "Y"(급행)
    // TAGO API: exprnYn = "N"(일반), "Y"(급행)
    // 두 API 모두 급행은 "Y"로 정규화하여 체크
    fun isExpressTrain(): Boolean = express.equals("Y", ignoreCase = true)
}
