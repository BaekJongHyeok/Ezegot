package com.jonghyeok.ezegot.api

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root


@Root(name = "SearchInfoBySubwayNameService", strict = false)
data class StationResponse(
    @field:Element(name = "RESULT")
    var result: Result = Result(), // 기본 생성자 추가

    @field:ElementList(name = "row", inline = true)
    var stationList: MutableList<StationInfoDTO> = mutableListOf() // 수정 가능한 리스트로 변경
)

@Root(name = "RESULT", strict = false)
data class Result(
    @field:Element(name = "CODE", required = false)
    var code: String = "", // 기본 생성자 추가

    @field:Element(name = "MESSAGE", required = false)
    var message: String = "" // 기본 생성자 추가
)

@Root(name = "row", strict = false)
data class StationInfoDTO(
    @field:Element(name = "STATION_CD", required = false)
    var stationCode: String = "", // 기본 생성자 추가

    @field:Element(name = "STATION_NM", required = false)
    var stationName: String = "", // 기본 생성자 추가

    @field:Element(name = "LINE_NUM", required = false)
    var lineNumber: String = "", // 기본 생성자 추가

    @field:Element(name = "FR_CODE", required = false)
    var frCode: String = "" // 기본 생성자 추가
)