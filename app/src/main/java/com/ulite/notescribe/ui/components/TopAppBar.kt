package com.ulite.notescribe.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopAppBar(
    title: String, // Title of the app bar
    navigationIcon: ImageVector? = null, // Navigation icon (optional)
    onNavigationClick: () -> Unit = {}, // Click event for the navigation icon
    actions: List<AppBarAction> = emptyList() // List of additional action buttons
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Navigation Icon"
                    )
                }
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.description
                    )
                }
            }
        },
        // Uncomment the following block to customize colors
        /*
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        */
    )
}

// Data model for action buttons (Actions)
data class AppBarAction(
    val icon: ImageVector, // Icon for the button
    val description: String, // Description of the button
    val onClick: () -> Unit // Click event for the button
)