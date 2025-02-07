package com.jonghyeok.ezegot.api

import com.jonghyeok.ezegot.dto.StationInfo
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "SearchInfoBySubwayNameService", strict = false)
data class StationResponse(
    @field:Element(name = "RESULT")
    var stationInfoResult: StationInfoResult = StationInfoResult(), // 기본 생성자 추가

    @field:ElementList(name = "row", inline = true)
    var stationList: MutableList<StationInfo> = mutableListOf() // 수정 가능한 리스트로 변경
)

@Root(name = "RESULT", strict = false)
data class StationInfoResult(
    @field:Element(name = "CODE", required = false)
    var code: String = "", // 기본 생성자 추가

    @field:Element(name = "MESSAGE", required = false)
    var message: String = "" // 기본 생성자 추가
)