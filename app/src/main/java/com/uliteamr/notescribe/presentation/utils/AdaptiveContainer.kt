package com.uliteamr.notescribe.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdaptiveLayoutContainer(
    modifier: Modifier = Modifier,
    compact: @Composable () -> Unit,
    medium: (@Composable () -> Unit)? = null,
    expanded: (@Composable () -> Unit)? = null,
) {
    val currentSizeGroup = LocalWindowSizeGroup.current

    Box(modifier = modifier) {
        when (currentSizeGroup) {
            WindowSizeGroup.EXPANDED -> (expanded ?: medium ?: compact).invoke()
            WindowSizeGroup.MEDIUM -> (medium ?: compact).invoke()
            WindowSizeGroup.COMPACT -> compact()
        }
    }
}
