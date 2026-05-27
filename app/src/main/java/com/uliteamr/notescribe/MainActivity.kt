package com.uliteamr.notescribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.uliteamr.notescribe.presentation.screens.root.RootLayout
import com.uliteamr.notescribe.presentation.theme.NoteScribeTheme
import com.uliteamr.notescribe.presentation.utils.WindowSizeProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteScribeTheme {
                WindowSizeProvider {
                    RootLayout()
                }
            }
        }
    }
}

@Preview
@Composable
fun preview(){
    NoteScribeTheme {
        WindowSizeProvider {
            RootLayout()
        }
    }
}
