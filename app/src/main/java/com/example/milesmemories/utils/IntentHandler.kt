package com.example.milesmemories.utils

import android.content.Intent

/**
 * Utility object to process incoming intents, such as shared text from other applications.
 */
object IntentHandler {
    
    /**
     * Parses the intent and updates the shared location state if applicable.
     */
    fun handleIntent(intent: Intent?) {
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
}
