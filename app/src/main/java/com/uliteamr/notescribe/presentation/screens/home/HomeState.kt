package com.uliteamr.notescribe.presentation.screens.home

data class HomeState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val isEmpty: Boolean = true,
)
