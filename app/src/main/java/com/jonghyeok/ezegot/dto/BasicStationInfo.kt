package com.jonghyeok.ezegot.dto

// 역 이름과 노선만 들어있는 DTO
data class BasicStationInfo(
    val stationName: String,
    val lineNumber: String,
    val distance: Double? = null
)
