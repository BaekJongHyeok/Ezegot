package com.jonghyeok.ezegot.api

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

/**
 * 첫차 막차 시간표 응답 (서울 열린데이터 광장)
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
    var express: String = "" // 서울 API는 "D"(급행) / "G"(일반), TAGO는 "Y" / "N"
) {
    /**
     * 급행 여부.
     *
     * 서울 API는 "Y"를 주지 않는다. 실측 값은 "D"와 "G"뿐이다.
     * 9호선 고속터미널(급행 정차)은 D 118 / G 125인데 사평(급행 미정차)은 G 125뿐이고,
     * 급행이 없는 7호선 강남구청은 G 200뿐이다. 즉 D가 급행이다.
     * "Y"만 보고 있어서 급행 표식이 한 번도 나오지 않았다.
     *
     * TAGO 폴백은 "Y"/"N"을 주므로 두 표기를 모두 받는다.
     */
    fun isExpressTrain(): Boolean =
        express.equals("D", ignoreCase = true) || express.equals("Y", ignoreCase = true)
}
