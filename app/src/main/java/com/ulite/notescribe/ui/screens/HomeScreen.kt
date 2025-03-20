package com.ulite.notescribe.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ulite.notescribe.ui.components.NotesTopAppBar
import com.ulite.notescribe.ui.components.AppBarAction

@Composable
fun HomeScreen(
    onNavigateBack: () -> Unit,
    onSearchClicked: () -> Unit,
    onMenuClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            NotesTopAppBar(
                title = "Home",
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = onNavigateBack,
                actions = listOf(
                    AppBarAction(
                        icon = Icons.Default.Search,
                        description = "Search",
                        onClick = onSearchClicked
                    ),
                    AppBarAction(
                        icon = Icons.Default.MoreVert,
                        description = "More Options",
                        onClick = onMenuClicked
                    )
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Welcome to the Home Screen!",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}