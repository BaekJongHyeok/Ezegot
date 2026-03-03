package com.jonghyeok.ezegot.viewModel

import androidx.lifecycle.viewModelScope
import com.jonghyeok.ezegot.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : BaseViewModel() {

    init {
        loadAllStations()
    }

    private fun loadAllStations() {
        viewModelScope.launch {
            val stations = mainRepository.getAllStations()
            if (stations.isNotEmpty()) {
                setAllStations(stations)
            }
            setLoadingState(false)
        }
    }
}