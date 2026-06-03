package com.example.milesmemories

import android.app.Application
import com.cloudinary.android.MediaManager

class MilesMemoriesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val config = mapOf(
            "cloud_name" to "doi4tnwbb",
            "api_key" to "592254132171869",
            "api_secret" to "HtibbCzk9J1TCBhIejLsM74qHxI",
            "secure" to true
        )
        
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Usually means already initialized
        }
    }
}
