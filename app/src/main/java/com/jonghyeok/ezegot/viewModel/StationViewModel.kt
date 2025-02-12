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

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isNotification = MutableStateFlow(false)
    val isNotification: StateFlow<Boolean> = _isNotification.asStateFlow()


    // 역 기초 정보 설정
    fun loadStationInfo(stationName: String, line: String) {
        val stationInfo = BasicStationInfo(stationName, line)
        _stationInfo.value = stationInfo
        _isFavorite.value = repository.isStationSaved(stationInfo)
        _isNotification.value = repository.isNotification(stationInfo)
    }

    // 실시간 도착 정보 조회
    fun loadArrivalInfo(stationName: String) {
        viewModelScope.launch {
            _arrivalInfo.value = repository.getRealtimeArrivalInfo(stationName)
        }
    }

    // 즐겨찾기 추가/삭제 토글
    fun toggleFavorite() {
        val station = _stationInfo.value ?: return
        if (_isFavorite.value) {
            repository.removeStation(station)
        } else {
            repository.addStation(station)
        }
        _isFavorite.value = !_isFavorite.value
    }

    // 알람 추가/ 삭제 토글
    fun toggleNotification() {
        val station = _stationInfo.value ?: return
        if (_isNotification.value) {
            repository.removeNotification(station)
        } else {
            repository.addNotification(station)
        }
        _isNotification.value = !_isNotification.value

    }
}