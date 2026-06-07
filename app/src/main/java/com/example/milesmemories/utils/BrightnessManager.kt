package com.example.milesmemories.utils

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Manages the device's light sensor to adjust screen brightness dynamically.
 */
class BrightnessManager(private val activity: Activity) : SensorEventListener {
    private var sensorManager: SensorManager? = activity.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    init {
        if (lightSensor == null) {
            Log.w("BrightnessManager", "Light sensor not found on this device.")
        }
    }

    /**
     * Registers the light sensor listener. Should be called in Activity.onResume.
     */
    fun register() {
        lightSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Unregisters the light sensor listener. Should be called in Activity.onPause.
     */
    fun unregister() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            val maxLux = 1000f
            val normalizedLux = (lux / maxLux).coerceIn(0.1f, 1.0f)

            val layoutParams = activity.window.attributes
            layoutParams.screenBrightness = normalizedLux
            activity.window.attributes = layoutParams
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }
}
