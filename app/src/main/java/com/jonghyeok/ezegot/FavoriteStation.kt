package com.jonghyeok.ezegot

data class FavoriteStation(
    val stationName: String,
    val directions: List<String>, // 방면들
    val times: List<String>, // 각 방면에 대한 남은 시간들
    val imageRes: Int // 이미지 리소스 ID 추가
)
