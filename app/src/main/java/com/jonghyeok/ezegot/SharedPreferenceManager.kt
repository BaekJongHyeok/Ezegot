package com.jonghyeok.ezegot

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.StationInfo

class SharedPreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("stations_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // =============================================================================================
    // 전체 역 정보
    // =============================================================================================
    /// 역 정보 가져오기
    fun getStationList(): List<StationInfo> {
        val json = prefs.getString("stationList", null)
        return if (json != null) {
            val gson = Gson()
            val stationListType = object : TypeToken<List<StationInfo>>() {}.type
            gson.fromJson(json, stationListType)
        } else {
            emptyList() // 예외 처리: 데이터가 없으면 빈 리스트 반환
        }
    }

    /// 역 정보 저장
    fun saveAllStationList(allStationList: List<StationInfo>) {
        val json = gson.toJson(allStationList) // List<Station>을 JSON 문자열로 변환
        prefs.edit().putString("stationList", json).apply()
    }


    // =============================================================================================
    // 즐겨찾기
    // =============================================================================================
    /// 즐겨찾기 목록 불러오기
    fun getSavedStations(): Set<BasicStationInfo> {
        val jsonString = prefs.getString("saved_stations", "[]") // 기본값: 빈 배열 JSON
        val type = object : TypeToken<Set<StationInfo>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptySet()
    }

    /// 즐겨찾기 추가
    fun saveStation(stationInfo: BasicStationInfo) {
        val stations = getSavedStations().toMutableSet()
        stations.add(stationInfo)
        val jsonString = gson.toJson(stations)
        prefs.edit().putString("saved_stations", jsonString).apply()
    }

    /// 즐겨찾기 삭제
    fun removeStation(stationInfo: BasicStationInfo) {
        val stations = getSavedStations().toMutableSet()
        stations.remove(stationInfo)
        val jsonString = gson.toJson(stations)
        prefs.edit().putString("saved_stations", jsonString).apply()
    }

    /// 특정 역이 즐겨찾기 되어 있는지 확인
    fun isStationSaved(stationInfo: BasicStationInfo): Boolean {
        return getSavedStations().contains(stationInfo)
    }

    // =============================================================================================
    // 최근검색
    // =============================================================================================
    /// 최근 검색 역 목록 불러오기
    fun getRecentSearches(): List<RecentSearchItem> {
        val jsonString = prefs.getString("recentSearchList", "[]")
        val type = object : TypeToken<List<RecentSearchItem>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    /// 최근 검색 역 목록 저장
    fun saveRecentSearchList(updatedList: List<RecentSearchItem>) {
        val updatedJson = gson.toJson(updatedList)
        prefs.edit().putString("recentSearchList", updatedJson).apply()
    }
}
