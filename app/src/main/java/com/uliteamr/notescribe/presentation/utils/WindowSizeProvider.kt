package com.uliteamr.notescribe.presentation.utils

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

/**
 * Enumeration defining the supported window size classifications based on Material 3 design tokens.
 * Used to drive conditional UI logic across the application.
 */
enum class WindowSizeGroup {
    /** Mobile portrait or narrow split-screen. Width < 600dp. */
    COMPACT,
    /** Tablets in portrait or foldables. Width between 600dp and 839dp. */
    MEDIUM,
    /** Desktop monitors or tablets in landscape. Width >= 840dp. */
    EXPANDED
}

/**
 * Global configuration constants for layout breakpoints.
 * Centralizing these values ensures consistency during White-label adjustments.
 */
object AdaptiveThresholds {
    val Medium = 600.dp
    val Expanded = 840.dp
}

/**
 * A [CompositionLocal] used to propagate the current [WindowSizeGroup] down the UI tree.
 * Defaults to [WindowSizeGroup.COMPACT] to ensure safety during IDE Previews.
 */
val LocalWindowSizeGroup = staticCompositionLocalOf { WindowSizeGroup.COMPACT }

/**
 * The Root Provider for adaptive layouts.
 * It uses [BoxWithConstraints] to perform sub-composition and determine the available
 * screen real estate without relying on platform-specific configuration APIs.
 *
 * @param content The composable hierarchy that will consume the [LocalWindowSizeGroup].
 */
@Composable
fun WindowSizeProvider(content: @Composable () -> Unit) {
    BoxWithConstraints {
        // Optimization: Re-calculate only when the maximum width constraint changes.
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