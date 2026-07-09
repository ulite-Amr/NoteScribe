package com.uliteamr.notescribe.presentation.screens.home

sealed interface HomeEvent {
    data object OnAddNoteClick : HomeEvent
    data object OnNavigatedToCreate : HomeEvent
}
