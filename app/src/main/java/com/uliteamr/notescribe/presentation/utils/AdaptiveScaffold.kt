package com.uliteamr.notescribe.presentation.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A high-level Scaffold that automatically reconfigures the UI architecture:
 * - On [WindowSizeGroup.COMPACT]: Traditional Mobile UI with Top/Bottom bars.
 * - On [WindowSizeGroup.MEDIUM/EXPANDED]: Tablet/Desktop UI with a Side Navigation Rail/Drawer.
 *
 * @param topBar Universal top application bar. Nullable to allow full-screen pages (e.g., Login).
 * @param bottomBar Navigation bar visible ONLY on compact screens. Nullable.
 * @param navigationRail Navigation Rail visible ONLY on medium screens. Nullable.
 * @param navigationDrawer Side Navigation Drawer visible ONLY on expanded screens. Nullable.
 * @param content The main screen content. Receives a [Modifier] that handles
 * dynamic system insets (padding) correctly across all layouts.
 */
@Composable
fun AdaptiveScaffold(
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    navigationRail: (@Composable () -> Unit)? = null,
    navigationDrawer: (@Composable () -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    AdaptiveLayoutContainer(
        compact = {
            Scaffold(
                topBar = { topBar?.invoke() },
                bottomBar = { bottomBar?.invoke() },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { padding ->
                content(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
        },
        medium = {
            Row(modifier = Modifier.fillMaxSize()) {
                // Will not occupy any space if navigationRail is null
                navigationRail?.invoke()

                Scaffold(
                    topBar = { topBar?.invoke() },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.weight(1f)
                ) { padding ->
                    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        content(Modifier.fillMaxSize())
                    }
                }
            }
        },
        expanded = {
            Row(modifier = Modifier.fillMaxSize()) {
                // Renders the 240.dp box ONLY if navigationDrawer is explicitly provided
                if (navigationDrawer != null) {
                    Box(modifier = Modifier.width(240.dp)) {
                        navigationDrawer()
                    }
                }

                Scaffold(
                    topBar = { topBar?.invoke() },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.weight(1f)
                ) { padding ->
                    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        content(Modifier.fillMaxSize())
                    }
                }
            }
        }
    )
}