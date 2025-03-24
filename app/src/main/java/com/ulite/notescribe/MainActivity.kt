package com.ulite.notescribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ulite.notescribe.ui.screens.HomeScreen
import com.ulite.notescribe.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            AppTheme {
                HomeScreen(
                    uiState = uiState,
                    onEvent = { event -> viewModel.handleEvent(event) }
                )
            }
        }
    }
}






