package com.jonghyeok.ezegot.dto

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

// API 호출을 통해 역 정보를 받아올 때 사용되는 DTO
@Root(name = "row", strict = false)
data class StationInfo(
    @field:Element(name = "STATION_CD", required = false)
    var stationCode: String = "", // 기본 생성자 추가

    @field:Element(name = "STATION_NM", required = false)
    var stationName: String = "", // 기본 생성자 추가

    @field:Element(name = "LINE_NUM", required = false)
    var lineNumber: String = "", // 기본 생성자 추가

    @field:Element(name = "FR_CODE", required = false)
    var frCode: String = "" // 기본 생성자 추가
)