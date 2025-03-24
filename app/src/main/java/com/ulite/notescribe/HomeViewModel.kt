package com.ulite.notescribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // الحالة الخاصة بالشاشة
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    // الأحداث (Events)
    fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.FabClicked -> handleFabClick()
            is HomeEvent.SearchClicked -> handleSearch()
            is HomeEvent.MenuClicked -> handleMenuClick()
        }
    }

    private fun handleFabClick() {
        viewModelScope.launch {
            /* مثال: تحديث الحالة أو تنفيذ عمل معين
            _uiState.value = HomeUiState.ShowDialog
             */
        }
    }

    private fun handleSearch() {
        // معالجة حدث البحث
    }

    private fun handleMenuClick() {
        // معالجة حدث الضغط على القائمة هنا
        viewModelScope.launch {
            /* مثال: تحديث الحالة أو تنفيذ عمل معين
            _uiState.value = HomeUiState.ShowMenuOptions
             */
        }
    }
}