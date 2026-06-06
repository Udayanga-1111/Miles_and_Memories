package com.example.milesmemories.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.milesmemories.ui.components.StatusBarColor
import com.example.milesmemories.ui.services.Navigation
import com.example.milesmemories.ui.theme.MilesMemoriesTheme

/**
 * Root composable hosting theme configuration and navigation graph.
 */
@Composable
fun AppRoot() {
    val systemTheme = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemTheme) }

    MilesMemoriesTheme(darkTheme = isDarkTheme) {
        StatusBarColor(darkIcons = !isDarkTheme)

        Box(modifier = Modifier.fillMaxSize()) {
            Navigation(
                isDarkTheme = isDarkTheme,
                onThemeChange = { isDarkTheme = it }
            )
        }
    }
}
