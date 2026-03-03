package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.api.StationInfoResponse
import com.jonghyeok.ezegot.dto.StationInfo
import com.jonghyeok.ezegot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    private val _allStationsInfoList = MutableStateFlow<List<StationInfo>>(emptyList())
    val allStationsInfoList: StateFlow<List<StationInfo>> = _allStationsInfoList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loadingState = MutableStateFlow(true)
    val loadingState: StateFlow<Boolean> get() = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> get() = _errorState

    protected fun setLoading(value: Boolean) { _isLoading.value = value }
    protected fun setLoadingState(value: Boolean) { _loadingState.value = value }
    protected fun setError(msg: String?) { _errorState.value = msg }
    protected fun setAllStations(list: List<StationInfo>) { _allStationsInfoList.value = list }
}
