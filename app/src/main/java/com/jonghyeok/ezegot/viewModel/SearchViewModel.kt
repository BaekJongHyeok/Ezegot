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
    }

    // 역 전체 리스트
    fun getStationList() {
        _stationList.value = repository.getStationList()
    }

    // 최근 검색 기록 추가
    fun saveRecentSearch(item: RecentSearchItem) {
        _recentSearches.value = _recentSearches.value.plus(item)
        repository.saveRecentSearchList(_recentSearches.value)
    }

    // 최근 검색 기록 삭제
    fun removeRecentSearch(item: RecentSearchItem) {
        _recentSearches.value = _recentSearches.value.minus(item)
        repository.saveRecentSearchList(_recentSearches.value)
    }

    // 최근 검색 기록 조회
    fun loadRecentSearches() {
        _recentSearches.value = repository.loadRecentSearches().orEmpty()
    }

    fun filterStations(query: String) {
        _filteredStations.value = _stationList.value.filter {
            it.stationName.contains(query, ignoreCase = true)
        }
    }
}