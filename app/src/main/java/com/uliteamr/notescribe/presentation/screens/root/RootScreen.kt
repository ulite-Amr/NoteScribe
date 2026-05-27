package com.uliteamr.notescribe.presentation.screens.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uliteamr.notescribe.presentation.screens.home.HomeScreen
import com.uliteamr.notescribe.presentation.utils.AdaptiveScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootLayout(modifier: Modifier = Modifier) {
    AdaptiveScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NoteScribe",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
    ) { childModifier ->
        HomeScreen(modifier = childModifier.fillMaxSize())
    }
}
