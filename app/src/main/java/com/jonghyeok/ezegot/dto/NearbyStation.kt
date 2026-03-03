package com.jonghyeok.ezegot.dto

data class NearbyStation(
    val stationName: String,
    val lineNumber: String,
    val distance: Double,
    val latitude: Double,
    val longitude: Double
)
