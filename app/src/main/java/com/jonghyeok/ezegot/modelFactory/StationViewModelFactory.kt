package com.jonghyeok.ezegot.modelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jonghyeok.ezegot.repository.StationRepository
import com.jonghyeok.ezegot.viewModel.StationViewModel

class StationViewModelFactory(private val repository: StationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}