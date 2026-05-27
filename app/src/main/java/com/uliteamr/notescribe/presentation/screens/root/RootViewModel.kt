package com.uliteamr.notescribe.presentation.screens.root

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RootViewModel : ViewModel() {

    private val _state = MutableStateFlow(RootState())
    val state: StateFlow<RootState> = _state.asStateFlow()

    fun onEvent(event: RootEvent) {
        when (event) {
            RootEvent.OnNavigateBack -> { /* handled by navController */ }
        }
    }

    fun updateTopBarTitle(title: String) {
        _state.update { it.copy(topBarTitle = title) }
    }

    fun updateCurrentRoute(route: String?) {
        _state.update { it.copy(currentRoute = route) }
    }
}
