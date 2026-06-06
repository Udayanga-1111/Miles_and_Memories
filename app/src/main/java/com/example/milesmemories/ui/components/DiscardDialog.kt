package com.example.milesmemories.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.milesmemories.models.Screen

/**
 * Discard action and confirmation dialog for the Add/Edit Note screen.
 */

@Composable
fun DiscardButton(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    Text(
        text = "Discard",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .padding(8.dp)
            .clickable { showDialog = true }
    )

    DiscardConfirmDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onConfirm = {
            showDialog = false
            navController.navigate(Screen.HomePage.route)
        }
    )
}

@Composable
fun DiscardConfirmDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Discard") },
        text = { Text("Are you sure you want to Discard?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Discard", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
