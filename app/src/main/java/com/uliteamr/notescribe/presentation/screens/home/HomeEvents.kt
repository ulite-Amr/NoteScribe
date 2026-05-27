package com.uliteamr.notescribe.presentation.screens.home

sealed class HomeEvent {
    data object OnAddNoteClick : HomeEvent()
}
