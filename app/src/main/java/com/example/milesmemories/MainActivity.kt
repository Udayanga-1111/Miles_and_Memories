@file:Suppress("DEPRECATION")

package com.example.milesmemories

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.milesmemories.ui.components.StatusBarColor
import com.example.milesmemories.ui.services.Navigation
import com.example.milesmemories.ui.theme.MilesMemoriesTheme
import com.example.milesmemories.utils.BrightnessManager
import com.example.milesmemories.utils.IntentHandler
import com.example.milesmemories.utils.WeatherManager

/**
 * Main entry point of the application.
 * Sets up the UI, handles deep linking/intents, and manages brightness sensing.
 */
class MainActivity : FragmentActivity() {
    private lateinit var brightnessManager: BrightnessManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IntentHandler.handleIntent(intent)
        
        WeatherManager.startNetworkCallback(this)
        brightnessManager = BrightnessManager(this)

        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(systemTheme)
            }
            
            MilesMemoriesTheme(darkTheme = isDarkTheme) {
                StatusBarColor(darkIcons = !isDarkTheme)
                
                Box(modifier = Modifier.fillMaxSize()) {
                    Navigation(isDarkTheme, onThemeChange = { isDarkTheme = it })
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        IntentHandler.handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::brightnessManager.isInitialized) {
            brightnessManager.register()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::brightnessManager.isInitialized) {
            brightnessManager.unregister()
        }
    }
}
