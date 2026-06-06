package com.example.milesmemories.ui.components

import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration as OsmConfig
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

private const val DEFAULT_LAT = 6.9271
private const val DEFAULT_LON = 79.8612

/**
 * Full-screen map dialog for selecting and confirming a journey location.
 */
@Composable
fun MapPickerDialog(
    currentLocation: String,
    onDismiss: () -> Unit,
    onLocationConfirmed: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mapMarkerLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var mapSearchQuery by remember { mutableStateOf("") }
    var isSearchingLocation by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var mapMarkerRef by remember { mutableStateOf<Marker?>(null) }

    fun searchLocation() {
        if (mapSearchQuery.isBlank()) return

        isSearchingLocation = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocationName(mapSearchQuery, 1)
                if (!results.isNullOrEmpty()) {
                    val result = results[0]
                    val point = GeoPoint(result.latitude, result.longitude)
                    withContext(Dispatchers.Main) {
                        mapMarkerLocation = point
                        mapViewRef?.controller?.animateTo(point)
                        mapMarkerRef?.position = point
                        mapViewRef?.invalidate()
                        isSearchingLocation = false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isSearchingLocation = false
                        Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSearchingLocation = false
                    Toast.makeText(context, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun resolveAddress(geoPoint: GeoPoint, onResolved: (String) -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val addressName = try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    formatAddress(addresses[0], geoPoint)
                } else {
                    "${geoPoint.latitude},${geoPoint.longitude}"
                }
            } catch (_: Exception) {
                "${geoPoint.latitude},${geoPoint.longitude}"
            }

            withContext(Dispatchers.Main) {
                onResolved(addressName)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pick Location",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = mapSearchQuery,
                        onValueChange = { mapSearchQuery = it },
                        placeholder = { Text("Search location...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { searchLocation() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { searchLocation() },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    ) {
                        if (isSearchingLocation) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        OsmConfig.getInstance().userAgentValue = ctx.packageName
                        val mapView = MapView(ctx)
                        mapView.setMultiTouchControls(true)
                        mapView.controller.setZoom(15.0)

                        val startPoint = parseLocationToGeoPoint(currentLocation)
                            ?: GeoPoint(DEFAULT_LAT, DEFAULT_LON)
                        mapView.controller.setCenter(startPoint)

                        val marker = Marker(mapView)
                        marker.position = startPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(marker)
                        mapMarkerLocation = startPoint
                        mapMarkerRef = marker
                        mapViewRef = mapView

                        val receiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(point: GeoPoint?): Boolean {
                                point?.let {
                                    marker.position = it
                                    mapMarkerLocation = it
                                    mapView.invalidate()
                                }
                                return true
                            }

                            override fun longPressHelper(point: GeoPoint?): Boolean = false
                        }
                        mapView.overlays.add(MapEventsOverlay(receiver))

                        mapView
                    },
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        val geoPoint = mapMarkerLocation
                        if (geoPoint != null) {
                            resolveAddress(geoPoint) { address ->
                                onLocationConfirmed(address)
                                onDismiss()
                            }
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                ) {
                    Text("Confirm Location", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

private fun parseLocationToGeoPoint(location: String): GeoPoint? {
    if (location.isBlank() || !location.contains(",")) return null
    val parts = location.split(",")
    val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
    val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
    return if (lat != null && lng != null) GeoPoint(lat, lng) else null
}

private fun formatAddress(addr: android.location.Address, geoPoint: GeoPoint): String {
    val feature = addr.featureName
    val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
    val isNumeric = feature?.matches(Regex("\\d+[a-zA-Z]*(-?\\d+[a-zA-Z]*)?")) == true

    return when {
        !feature.isNullOrBlank() && !isNumeric && feature != city -> {
            if (!city.isNullOrBlank()) "$feature, $city" else feature
        }
        !city.isNullOrBlank() -> city
        else -> addr.getAddressLine(0) ?: "${geoPoint.latitude},${geoPoint.longitude}"
    }
}
