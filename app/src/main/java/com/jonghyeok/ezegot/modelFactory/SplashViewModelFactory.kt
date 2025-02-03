package com.jonghyeok.ezegot.modelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jonghyeok.ezegot.repository.SplashRepository
import com.jonghyeok.ezegot.viewModel.SplashViewModel

class SplashViewModelFactory (private val repository: SplashRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SplashViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}