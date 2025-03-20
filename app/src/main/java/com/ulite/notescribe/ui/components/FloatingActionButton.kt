package com.ulite.notescribe.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

// Default FAB with Icon
@Composable
fun IconFAB(
    icon: ImageVector = Icons.Default.Add, // Default icon is "Add"
    contentDescription: String = "Floating Action Button", // Default description
    onClick: () -> Unit // Click event
) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

// Extended FAB with Icon and Text
@Composable
fun ExtendedFAB(
    icon: ImageVector = Icons.Default.Add, // Default icon is "Add"
    text: String = "Add", // Text to display
    contentDescription: String = "Extended Floating Action Button", // Default description
    onClick: () -> Unit // Click event
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        },
        text = { Text(text = text) }
    )
}

