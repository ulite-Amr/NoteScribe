package com.ulite.notescribe.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ulite.notescribe.HomeEvent
import com.ulite.notescribe.HomeUiState
import com.ulite.notescribe.ui.components.AppBarAction
import com.ulite.notescribe.ui.components.NotesTopAppBar


@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit
) {
    // مراقبة التغييرات في الحالة
    when (uiState) {
        HomeUiState.Idle -> {

        }

        is HomeUiState.Error -> TODO()
    }


    // بناء الواجهة
    Scaffold(
        topBar = {
            val onMenuClicked = null
            NotesTopAppBar(
                title = "Home",
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = {  },
                actions = listOf(
                    AppBarAction(
                        icon = Icons.Default.Search,
                        description = "Search",
                        onClick = {}
                    ),
                    AppBarAction(
                        icon = Icons.Default.MoreVert,
                        description = "More Options",
                        onClick = {}
                    )
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(HomeEvent.FabClicked) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Welcome to the Home Screen!",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


