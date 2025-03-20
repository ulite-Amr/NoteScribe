package com.ulite.notescribe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        // content
        Text(text = "Home Screen")

    }

}