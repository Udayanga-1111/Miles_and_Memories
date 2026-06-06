package com.example.milesmemories.utils

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Adjusts screen brightness based on ambient light sensor readings.
 */
class LightSensorController(
    private val activity: Activity,
    sensorManager: SensorManager?
) : SensorEventListener {

    private val sensorManager: SensorManager? = sensorManager
    private val lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    init {
        if (lightSensor == null) {
            Log.w(TAG, "Light sensor not found on this device.")
        }
    }

    fun onResume() {
        lightSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun onPause() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_LIGHT) return

        val normalizedLux = (event.values[0] / MAX_LUX).coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = normalizedLux
        activity.window.attributes = layoutParams
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        private const val TAG = "LightSensorController"
        private const val MAX_LUX = 1000f
        private const val MIN_BRIGHTNESS = 0.1f
        private const val MAX_BRIGHTNESS = 1.0f
    }
}
