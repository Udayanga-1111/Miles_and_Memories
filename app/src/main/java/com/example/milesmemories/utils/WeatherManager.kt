package com.example.milesmemories.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(
    val locationName: String,
    val temperature: Double,
    val isDay: Int,
    val weatherCode: Int,
    val windSpeed: Double,
    val timestamp: Long
)

object WeatherManager {
    private const val CACHE_FILE_NAME = "weather_cache.json"

    val defaultLocations = listOf("London", "Tokyo", "New York", "Paris")

    suspend fun fetchWeather(context: Context, locationQuery: String): WeatherData? = withContext(Dispatchers.IO) {
        try {
            // 1. Geocoding
            val geoUrl = URL("https://geocoding-api.open-meteo.com/v1/search?name=${locationQuery.replace(" ", "+")}&count=1")
            val geoConn = geoUrl.openConnection() as HttpURLConnection
            geoConn.requestMethod = "GET"
            val geoResponse = geoConn.inputStream.bufferedReader().readText()
            val geoJson = JSONObject(geoResponse)
            if (!geoJson.has("results")) return@withContext null
            val results = geoJson.getJSONArray("results")
            if (results.length() == 0) return@withContext null
            
            val firstResult = results.getJSONObject(0)
            val lat = firstResult.getDouble("latitude")
            val lon = firstResult.getDouble("longitude")
            val resolvedName = firstResult.getString("name")

            // 2. Weather
            val weatherUrl = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true")
            val weatherConn = weatherUrl.openConnection() as HttpURLConnection
            weatherConn.requestMethod = "GET"
            val weatherResponse = weatherConn.inputStream.bufferedReader().readText()
            val weatherJson = JSONObject(weatherResponse)
            
            if (weatherJson.has("current_weather")) {
                val current = weatherJson.getJSONObject("current_weather")
                val temp = current.getDouble("temperature")
                val weatherCode = current.getInt("weathercode")
                val isDay = current.optInt("is_day", 1)
                val windSpeed = current.optDouble("windspeed", 0.0)
                
                val weatherData = WeatherData(
                    locationName = resolvedName,
                    temperature = temp,
                    isDay = isDay,
                    weatherCode = weatherCode,
                    windSpeed = windSpeed,
                    timestamp = System.currentTimeMillis()
                )
                
                // Save to cache
                saveToCache(context, weatherData)
                return@withContext weatherData
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun fetchDefaultWeathers(context: Context): List<WeatherData> = withContext(Dispatchers.IO) {
        val results = mutableListOf<WeatherData>()
        for (loc in defaultLocations) {
            val w = fetchWeather(context, loc)
            if (w != null) {
                results.add(w)
            }
        }
        results
    }

    private fun saveToCache(context: Context, data: WeatherData) {
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            val cacheObject = if (file.exists()) JSONObject(file.readText()) else JSONObject()
            
            val dataObj = JSONObject().apply {
                put("locationName", data.locationName)
                put("temperature", data.temperature)
                put("isDay", data.isDay)
                put("weatherCode", data.weatherCode)
                put("windSpeed", data.windSpeed)
                put("timestamp", data.timestamp)
            }
            cacheObject.put(data.locationName.lowercase(), dataObj)
            
            file.writeText(cacheObject.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFromCache(context: Context, query: String? = null): List<WeatherData> {
        try {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            if (!file.exists()) return emptyList()
            
            val cacheObject = JSONObject(file.readText())
            val results = mutableListOf<WeatherData>()
            
            if (query != null && query.isNotBlank()) {
                val qLower = query.lowercase()
                cacheObject.keys().forEach { key ->
                    if (key.contains(qLower)) {
                        val obj = cacheObject.getJSONObject(key)
                        results.add(parseWeatherData(obj))
                    }
                }
            } else {
                // If no query, load defaults if they exist in cache
                defaultLocations.forEach { loc ->
                    val key = loc.lowercase()
                    if (cacheObject.has(key)) {
                        results.add(parseWeatherData(cacheObject.getJSONObject(key)))
                    }
                }
                // If defaults aren't in cache, just return whatever is there
                if (results.isEmpty()) {
                    cacheObject.keys().forEach { key ->
                        if (results.size < 4) {
                            results.add(parseWeatherData(cacheObject.getJSONObject(key)))
                        }
                    }
                }
            }
            return results
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    private fun parseWeatherData(obj: JSONObject): WeatherData {
        return WeatherData(
            locationName = obj.getString("locationName"),
            temperature = obj.getDouble("temperature"),
            isDay = obj.getInt("isDay"),
            weatherCode = obj.getInt("weatherCode"),
            windSpeed = obj.getDouble("windSpeed"),
            timestamp = obj.getLong("timestamp")
        )
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    fun getWeatherDescription(code: Int): String {
        return when(code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snow fall"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }
}
