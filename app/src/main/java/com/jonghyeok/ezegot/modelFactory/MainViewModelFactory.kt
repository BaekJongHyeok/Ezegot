package com.jonghyeok.ezegot.modelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jonghyeok.ezegot.repository.LocationRepository
import com.jonghyeok.ezegot.repository.MainRepository
import com.jonghyeok.ezegot.viewModel.MainViewModel

class MainViewModelFactory(private val mainRepository: MainRepository, private val locationRepository: LocationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(mainRepository, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}