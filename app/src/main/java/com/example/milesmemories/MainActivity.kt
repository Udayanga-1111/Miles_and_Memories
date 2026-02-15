@file:Suppress("DEPRECATION")

package com.example.milesmemories

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.example.milesmemories.ui.services.Navigation
import com.example.milesmemories.ui.theme.MilesMemoriesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            // Checking the System Theme
            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember {
                if (systemTheme){
                    mutableStateOf(true)
                }else{
                    mutableStateOf(false)
                }
            }
            
            MilesMemoriesTheme(darkTheme = isDarkTheme) {
                StatusBarColor(darkIcons = !isDarkTheme)
                Navigation(isDarkTheme, onThemeChange = { isDarkTheme = it })

            }
        }
    }
}

@Composable
fun StatusBarColor(darkIcons: Boolean) {
    val setColor = MaterialTheme.colorScheme.primary
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = setColor.toArgb()
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = darkIcons
    }
}
