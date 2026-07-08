package com.uliteamr.notescribe.presentation.screens.root

sealed interface RootEvent {
    data object OnNavigateBack : RootEvent
    data class UpdateTopBarTitle(val title: String) : RootEvent
    data class UpdateCurrentRoute(val route: String?) : RootEvent
}