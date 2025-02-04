package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.repository.StationRepository
import com.jonghyeok.ezegot.dto.BasicStationInfo
import com.jonghyeok.ezegot.dto.RealtimeArrival
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StationViewModel(private val repository: StationRepository) : ViewModel() {

    private val _stationInfo = MutableStateFlow<BasicStationInfo?>(null)
    val stationInfo: StateFlow<BasicStationInfo?> = _stationInfo.asStateFlow()

    private val _arrivalInfo = MutableStateFlow<List<RealtimeArrival>>(emptyList())
    val arrivalInfo: StateFlow<List<RealtimeArrival>> = _arrivalInfo.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    /**
     * 역 기초 정보 설정
     */
    fun fetchStationInfo(stationName: String, line: String) {
        val stationInfo = BasicStationInfo(stationName, line)
        _stationInfo.value = stationInfo
        _isSaved.value = repository.isStationSaved(stationInfo) // !! 제거
    }


    /**
     * 실시간 도착 정보 조회
     */
    fun fetchRealtimeArrival(stationName: String) {
        viewModelScope.launch {
            _arrivalInfo.value = repository.getRealtimeArrivalInfo(stationName)
        }
    }

    /**
     * 즐겨찾기 등록 여부 확인
     */

    /**
     * 즐겨찾기 추가/삭제 토글
     */
    fun toggleFavorite() {
        val station = _stationInfo.value ?: return
        if (_isSaved.value) {
            repository.removeStation(station)
        } else {
            repository.saveStation(station)
        }
        _isSaved.value = !_isSaved.value
    }

}