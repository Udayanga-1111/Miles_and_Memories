/**
 * Screen displaying notification preferences.
 * Uses SharedPreferences to persist local user settings.
 */
package com.example.milesmemories.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsPage(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("miles_memories_prefs", Context.MODE_PRIVATE) }

    var journeyReminders by remember { mutableStateOf(prefs.getBoolean("pref_journey_reminders", true)) }
    var weeklySummary by remember { mutableStateOf(prefs.getBoolean("pref_weekly_summary", false)) }
    var appUpdates by remember { mutableStateOf(prefs.getBoolean("pref_app_updates", true)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Manage what notifications you receive from Miles & Memories.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            NotificationToggleRow(
                title = "Journey Reminders",
                subtitle = "Get reminded to document your travels",
                checked = journeyReminders,
                onCheckedChange = { 
                    journeyReminders = it 
                    prefs.edit().putBoolean("pref_journey_reminders", it).apply()
                }
            )

            NotificationToggleRow(
                title = "Weekly Summary",
                subtitle = "Receive a digest of your week's memories",
                checked = weeklySummary,
                onCheckedChange = { 
                    weeklySummary = it 
                    prefs.edit().putBoolean("pref_weekly_summary", it).apply()
                }
            )

            NotificationToggleRow(
                title = "App Updates & Tips",
                subtitle = "Stay informed about new features",
                checked = appUpdates,
                onCheckedChange = { 
                    appUpdates = it 
                    prefs.edit().putBoolean("pref_app_updates", it).apply()
                }
            )
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
