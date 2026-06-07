package com.example.milesmemories.utils

import android.content.Intent

/**
 * Processes share intents and forwards location text or map URLs
 * to [SharedLocationManager] for use on the Add Note screen.
 */
object ShareIntentHandler {

    private val URL_REGEX = "(https?://[a-zA-Z0-9./_?=-]+)".toRegex()

    fun handle(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        val match = URL_REGEX.find(sharedText)

        SharedLocationManager.pendingLocation.value = match?.value ?: sharedText
    }
}
