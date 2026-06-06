package com.example.milesmemories.ui.components

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Syncs the system status bar color and icon appearance with the active theme.
 */
@Composable
fun StatusBarColor(darkIcons: Boolean) {
    val statusBarColor = MaterialTheme.colorScheme.primary
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = statusBarColor.toArgb()
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = darkIcons
    }
}
