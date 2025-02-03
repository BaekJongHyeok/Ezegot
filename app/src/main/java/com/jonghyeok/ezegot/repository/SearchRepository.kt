package com.jonghyeok.ezegot.repository

import com.jonghyeok.ezegot.RecentSearchItem
import com.jonghyeok.ezegot.SharedPreferenceManager
import com.jonghyeok.ezegot.dto.StationInfo

class SearchRepository(
    private val pm: SharedPreferenceManager
) {

    fun getStationList(): List<StationInfo> {
        return pm.getStationList()
    }

    fun saveRecentSearchList(recentSearchList: List<RecentSearchItem>) {
        pm.saveRecentSearchList(recentSearchList)
    }

    fun removeRecentSearchList(recentSearchList: List<RecentSearchItem>) {

    }

    fun loadRecentSearches(): List<RecentSearchItem>? {
        return pm.getRecentSearches()
    }
}