package com.jonghyeok.ezegot.api

import com.google.gson.annotations.SerializedName

data class StationInfoResponse(
    @SerializedName("outStnNum") val stationId: String,
    @SerializedName("stnKrNm") val stationName: String,
    @SerializedName("lineNm") val lineName: String,
    @SerializedName("convX") val longitude: Double,
    @SerializedName("convY") val latitude: Double
)
