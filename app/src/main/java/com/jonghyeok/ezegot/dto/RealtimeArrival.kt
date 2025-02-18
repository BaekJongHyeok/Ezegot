package com.jonghyeok.ezegot.dto

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
)
