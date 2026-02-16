package com.example.milesmemories.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val datePickerState = rememberDatePickerState()

    // Function to handle date selection
    val handleConfirm = {
        onDateSelected(datePickerState.selectedDateMillis)
        onDismiss()
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = handleConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            // Hide title and headline in landscape to save vertical space
            title = if (isLandscape) null else {
                {
                    Text(
                        "Select date",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            },
            headline = if (isLandscape) null else {
                {
                    Text(
                        "Enter date",
                        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                    )
                }
            },
            showModeToggle = !isLandscape
        )
    }
}
