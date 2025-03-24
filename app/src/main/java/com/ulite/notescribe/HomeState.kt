package com.ulite.notescribe

sealed class HomeUiState {
    object Idle : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    // أضف أي حالات أخرى تحتاجها
}