package com.uliteamr.notescribe.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
