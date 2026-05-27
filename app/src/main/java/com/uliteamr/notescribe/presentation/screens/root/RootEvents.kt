package com.uliteamr.notescribe.presentation.screens.root

sealed class RootEvent {
    data object OnNavigateBack : RootEvent()
}