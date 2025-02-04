package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import com.jonghyeok.ezegot.RecentSearchItem
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel(private val repository: SearchRepository) : ViewModel() {
    private val _stationList = MutableStateFlow<List<StationInfo>>(emptyList())
    val stationList: StateFlow<List<StationInfo>> = _stationList.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<RecentSearchItem>>(emptyList())
    val recentSearches: StateFlow<List<RecentSearchItem>> = _recentSearches

    private val _filteredStations = MutableStateFlow<List<StationInfo>>(emptyList())
    val filteredStations: StateFlow<List<StationInfo>> = _filteredStations

    init {
        getStationList()
        loadRecentSearches()
    }

    // 역 전체 리스트
    fun getStationList() {
        _stationList.value = repository.getStationList()
    }

    // 최근 검색 기록 추가
    fun saveRecentSearch(item: RecentSearchItem) {
        val currentList = _recentSearches.value.toMutableList()

        // 중복 제거 (기존 리스트에서 동일한 항목 제거)
        currentList.removeAll { it.stationName == item.stationName && it.lineNumber == item.lineNumber }

        // 최신 검색을 맨 앞에 추가
        currentList.add(0, item)

        // 최대 20개까지만 유지
        val updatedList = currentList.take(20)

        // StateFlow 업데이트
        _recentSearches.value = updatedList

        // SharedPreferences에 저장 (전체 리스트를 저장)
        repository.saveRecentSearchList(updatedList)
    }


    // 최근 검색 기록 삭제
    fun removeRecentSearch(item: RecentSearchItem) {
        val updatedList = _recentSearches.value.filterNot {
            it.stationName == item.stationName && it.lineNumber == item.lineNumber
        }

        // StateFlow 업데이트
        _recentSearches.value = updatedList

        // SharedPreferences에 저장 (전체 리스트를 저장)
        repository.saveRecentSearchList(updatedList)
    }


    // 최근 검색 기록 조회
    private fun loadRecentSearches() {
        _recentSearches.value = repository.loadRecentSearches().orEmpty()
    }

    fun filterStations(query: String) {
        _filteredStations.value = _stationList.value.filter {
            it.stationName.contains(query, ignoreCase = true)
        }
    }
}