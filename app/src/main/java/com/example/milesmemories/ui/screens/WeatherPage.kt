/**
 * Screen displaying current weather data and forecast.
 * Supports searching by location and offline caching.
 */
package com.example.milesmemories.ui.screens

import android.content.res.Configuration
import com.example.milesmemories.ui.components.WeatherCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.milesmemories.ui.components.Header
import com.example.milesmemories.ui.components.NavigationBar
import com.example.milesmemories.ui.components.TitleHeader
import com.example.milesmemories.utils.WeatherData
import com.example.milesmemories.utils.WeatherManager
import kotlinx.coroutines.launch

@Composable
fun WeatherPage(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOnline by WeatherManager.isOnline.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var weatherList by remember { mutableStateOf<List<WeatherData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    fun refreshWeather() {
        isLoading = true
        coroutineScope.launch {
            if (isOnline) {
                if (searchQuery.isBlank()) {
                    weatherList = WeatherManager.fetchDefaultWeathers(context)
                } else {
                    val result = WeatherManager.fetchWeather(context, searchQuery)
                    weatherList = if (result != null) listOf(result) else emptyList()
                }
            } else {
                weatherList = WeatherManager.loadFromCache(context, searchQuery)
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit, isOnline) {
        refreshWeather()
    }

    Scaffold(
        topBar = {
            TitleHeader(
                searchBar = true,
                searchQuery = searchQuery,
                onSearchQueryChange = { 
                    searchQuery = it
                    refreshWeather()
                }
            )
        },
        bottomBar = {
            NavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Header("Weather", "")
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.padding(20.dp))
                        }
                    }
                } else if (weatherList.isEmpty()) {
                    item {
                        Text(
                            text = if (!isOnline) "No offline data available for this location." else "Location not found.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    items(weatherList) { weather ->
                        WeatherCard(weather = weather, isOffline = !isOnline)
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}


