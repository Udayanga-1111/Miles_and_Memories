package com.example.milesmemories.ui.screens

import android.content.res.Configuration
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
import com.example.milesmemories.ui.components.SearchBar
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

    var searchQuery by remember { mutableStateOf("") }
    var weatherList by remember { mutableStateOf<List<WeatherData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }

    fun refreshWeather() {
        isLoading = true
        coroutineScope.launch {
            val online = WeatherManager.isOnline(context)
            isOffline = !online

            if (online) {
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

    LaunchedEffect(Unit) {
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
            
            if (isOffline) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Offline Mode - Showing cached data",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isLandscape) {
                        Header("Weather", "")
                    } else {
                        Header("Weather", "")
                        Spacer(modifier = Modifier.height(10.dp))
                        SearchBar(query = searchQuery, onQueryChange = { 
                            searchQuery = it 
                            refreshWeather()
                        })
                    }
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
                            text = if (isOffline) "No offline data available for this location." else "Location not found.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    items(weatherList) { weather ->
                        WeatherCard(weather = weather, isOffline = isOffline)
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherData, isOffline: Boolean) {
    val description = WeatherManager.getWeatherDescription(weather.weatherCode)
    val timeDiff = System.currentTimeMillis() - weather.timestamp
    val hoursOld = timeDiff / (1000 * 60 * 60)
    val minutesOld = (timeDiff / (1000 * 60)) % 60
    
    val timeString = when {
        hoursOld > 0 -> "$hoursOld hr ${minutesOld} min ago"
        minutesOld > 0 -> "$minutesOld min ago"
        else -> "Just now"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = weather.locationName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                Icon(
                    imageVector = if (weather.isDay == 1) Icons.Default.WbSunny else Icons.Default.Cloud,
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(48.dp),
                    tint = if (weather.isDay == 1) Color(0xFFFFB300) else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${weather.temperature}°C",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "Wind Speed",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${weather.windSpeed} km/h",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (isOffline) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Data from: $timeString",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}
