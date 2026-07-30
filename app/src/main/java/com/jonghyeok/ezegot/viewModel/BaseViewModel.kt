package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import com.jonghyeok.ezegot.dto.StationInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel : ViewModel() {

    private val _allStationsInfoList = MutableStateFlow<List<StationInfo>>(emptyList())
    val allStationsInfoList: StateFlow<List<StationInfo>> = _allStationsInfoList

    private val _loadingState = MutableStateFlow(true)
    val loadingState: StateFlow<Boolean> get() = _loadingState

    protected fun setLoadingState(value: Boolean) { _loadingState.value = value }
    protected fun setAllStations(list: List<StationInfo>) { _allStationsInfoList.value = list }
}
