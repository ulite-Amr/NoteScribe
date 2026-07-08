package com.uliteamr.notescribe.presentation.utils

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

enum class WindowSizeGroup {
    COMPACT,
    MEDIUM,
    EXPANDED
}

object AdaptiveThresholds {
    val Medium = 600.dp
    val Expanded = 840.dp
}

val LocalWindowSizeGroup = staticCompositionLocalOf { WindowSizeGroup.COMPACT }

@Composable
fun WindowSizeProvider(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val windowSizeGroup = remember(maxWidth) {
            when {
                maxWidth < AdaptiveThresholds.Medium -> WindowSizeGroup.COMPACT
                maxWidth < AdaptiveThresholds.Expanded -> WindowSizeGroup.MEDIUM
                else -> WindowSizeGroup.EXPANDED
            }
        }

        CompositionLocalProvider(LocalWindowSizeGroup provides windowSizeGroup) {
            content()
        }
    }
}
