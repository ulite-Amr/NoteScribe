package com.uliteamr.notescribe.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A highly versatile container that manages UI state transitions between size groups.
 * * It implements a "Fallback Logic":
 * - If [expanded] is null, it tries [medium].
 * - If [medium] is null, it defaults to the mandatory [compact] layout.
 *
 * @param modifier Standard Compose modifier for layout adjustments.
 * @param compact The mandatory base layout (typically for mobile).
 * @param medium Optional layout for mid-sized displays (tablets).
 * @param expanded Optional layout for large displays (desktop).
 */
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