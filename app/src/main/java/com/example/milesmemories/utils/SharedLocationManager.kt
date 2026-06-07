package com.example.milesmemories.utils

import kotlinx.coroutines.flow.MutableStateFlow

object SharedLocationManager {
    val pendingLocation = MutableStateFlow<String?>(null)
}
