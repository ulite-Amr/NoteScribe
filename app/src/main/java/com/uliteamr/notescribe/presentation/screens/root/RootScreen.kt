package com.uliteamr.notescribe.presentation.screens.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uliteamr.notescribe.R
import com.uliteamr.notescribe.presentation.components.ContextMenuItem
import com.uliteamr.notescribe.presentation.components.TopBar
import com.uliteamr.notescribe.presentation.screens.home.HomeScreen
import com.uliteamr.notescribe.presentation.utils.AdaptiveScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootLayout(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        AdaptiveScaffold(
            topBar = {
                TopBar(
                    headerSlot = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    optionsMenuSlot = { closeMenu ->
                        Text(
                            text = "Workspace",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        ContextMenuItem(
                            label = "Settings",
                            onClick = { closeMenu() },
                            isCritical = true
                        )
                    }
                )
            },
        ) { childModifier ->
            HomeScreen(modifier = childModifier.fillMaxSize())
        }
    }
}
