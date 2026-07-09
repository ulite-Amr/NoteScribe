package com.uliteamr.notescribe.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnAddNoteClick -> onAddNoteClick()
            HomeEvent.OnNavigatedToCreate -> onNavigatedToCreate()
        }
    }

    private fun onAddNoteClick() {
        _state.update { it.copy(isNavigatingToCreate = true) }
    }

    private fun onNavigatedToCreate() {
        _state.update { it.copy(isNavigatingToCreate = false) }
    }
}
