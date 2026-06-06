@file:Suppress("DEPRECATION")

package com.example.milesmemories

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.example.milesmemories.ui.services.Navigation
import com.example.milesmemories.ui.theme.MilesMemoriesTheme

import android.content.Intent
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.milesmemories.utils.SharedLocationManager
import com.example.milesmemories.utils.WeatherManager

class MainActivity : FragmentActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        
        // Start monitoring network status
        WeatherManager.startNetworkCallback(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        
        if (lightSensor == null) {
            Log.w("MainActivity", "Light sensor not found on this device.")
        }

        enableEdgeToEdge()
        setContent {
            // Checking the System Theme
            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(systemTheme)
            }
            
            MilesMemoriesTheme(darkTheme = isDarkTheme) {
                StatusBarColor(darkIcons = !isDarkTheme)
                
                // Content fills the screen; status bar padding is handled by headers
                Box(modifier = Modifier.fillMaxSize()) {
                    Navigation(isDarkTheme, onThemeChange = { isDarkTheme = it })
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                val urlRegex = "(https?://[a-zA-Z0-9./_?=-]+)".toRegex()
                val match = urlRegex.find(sharedText)
                if (match != null) {
                    SharedLocationManager.pendingLocation.value = match.value
                } else {
                    SharedLocationManager.pendingLocation.value = sharedText
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            val maxLux = 1000f
            val normalizedLux = (lux / maxLux).coerceIn(0.1f, 1.0f)
            
            val layoutParams = window.attributes
            layoutParams.screenBrightness = normalizedLux
            window.attributes = layoutParams
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
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
