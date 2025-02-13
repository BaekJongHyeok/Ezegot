package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.dto.RecentSearchItem
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.dto.StationInfo

class SearchRepository(
    private val pm: SharedPreferenceManager
) {

    fun getStationList(): List<StationInfo> {
        return pm.getAllStationList()
    }

    fun saveRecentSearchList(station: List<RecentSearchItem>) {
        pm.saveRecentSearchList(station)
    }

    fun removeRecentSearchList(recentSearchList: List<RecentSearchItem>) {

    }

    fun loadRecentSearches(): List<RecentSearchItem>? {
        return pm.getRecentSearches()
    }
}