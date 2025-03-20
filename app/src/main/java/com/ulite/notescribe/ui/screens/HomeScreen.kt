package com.ulite.notescribe.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ulite.notescribe.ui.components.NotesTopAppBar
import com.ulite.notescribe.ui.components.AppBarAction
import com.ulite.notescribe.ui.components.ExtendedFAB


@Composable
fun HomeScreen(
    onNavigateBack: () -> Unit,
    onSearchClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    onFabClicked: () -> Unit
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
        },
        floatingActionButton = {
            ExtendedFAB (
                icon  = Icons.Default.Edit,
                text = "Create New Note",
            ){
                onFabClicked()
            }
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