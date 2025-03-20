package com.ulite.notescribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.ulite.notescribe.ui.screens.HomeScreen
import com.ulite.notescribe.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                HomeScreen(
                    onNavigateBack = {
                        // التعامل مع حدث الرجوع

                    },
                    onSearchClicked = {
                        // التعامل مع حدث البحث
                    },
                    onMenuClicked = {
                        // التعامل مع حدث القائمة
                    }
                )
            }
        }
    }
}
