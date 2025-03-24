package com.ulite.notescribe

sealed class HomeEvent {
    object FabClicked : HomeEvent(
        // الأحداث (Events)
    )
    object SearchClicked : HomeEvent()
    object MenuClicked : HomeEvent()
    // أضف أي أحداث أخرى
}