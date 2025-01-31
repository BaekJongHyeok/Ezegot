package com.jonghyeok.ezegot

import android.content.Context

class SharedPreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("stations_prefs", Context.MODE_PRIVATE)

    /// 즐겨찾기
    fun getSavedStations(): Set<String> {
        return prefs.getStringSet("saved_stations", emptySet()) ?: emptySet()
    }

    fun saveStation(station: String) {
        val stations = getSavedStations().toMutableSet()
        stations.add(station)
        prefs.edit().putStringSet("saved_stations", stations).apply()
    }

    fun removeStation(station: String) {
        val stations = getSavedStations().toMutableSet()
        stations.remove(station)
        prefs.edit().putStringSet("saved_stations", stations).apply()
    }

    fun isStationSaved(station: String): Boolean {
        return getSavedStations().contains(station)
    }
}