package com.jonghyeok.ezegot.modelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jonghyeok.ezegot.repository.SearchRepository
import com.jonghyeok.ezegot.repository.SharedRepository
import com.jonghyeok.ezegot.viewModel.SearchViewModel

class SearchViewModelFactory(private val sharedRepository: SharedRepository, private val repository: SearchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(sharedRepository, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}