package com.example.milesmemories.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController

const val PREF_NAME = "miles_memories_prefs"
const val PREF_BIOMETRIC_ENABLED = "biometric_enabled"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPage(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Read the persisted preference
    val prefs = remember { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }
    var isBiometricEnabled by remember { mutableStateOf(prefs.getBoolean(PREF_BIOMETRIC_ENABLED, false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Biometric Login",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBiometricEnabled)
                                "Fingerprint required on every app open"
                            else
                                "Use fingerprint to lock the app",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            // Enabling: verify with biometric first
                            val biometricManager = BiometricManager.from(context)
                            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                                BiometricManager.BIOMETRIC_SUCCESS -> {
                                    if (activity != null) {
                                        val executor = ContextCompat.getMainExecutor(context)
                                        val biometricPrompt = BiometricPrompt(activity, executor,
                                            object : BiometricPrompt.AuthenticationCallback() {
                                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                    super.onAuthenticationError(errorCode, errString)
                                                    Toast.makeText(context, "Authentication cancelled", Toast.LENGTH_SHORT).show()
                                                }
                                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                    super.onAuthenticationSucceeded(result)
                                                    // Persist the setting
                                                    prefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, true).apply()
                                                    isBiometricEnabled = true
                                                    Toast.makeText(context, "Biometric login enabled!", Toast.LENGTH_SHORT).show()
                                                }
                                                override fun onAuthenticationFailed() {
                                                    super.onAuthenticationFailed()
                                                    Toast.makeText(context, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                            .setTitle("Confirm your fingerprint")
                                            .setSubtitle("Verify to enable biometric login")
                                            .setNegativeButtonText("Cancel")
                                            .build()
                                        biometricPrompt.authenticate(promptInfo)
                                    }
                                }
                                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                                    Toast.makeText(context, "No biometric hardware available on this device.", Toast.LENGTH_LONG).show()
                                }
                                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                                    Toast.makeText(context, "Biometric hardware is currently unavailable.", Toast.LENGTH_LONG).show()
                                }
                                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                    Toast.makeText(context, "No fingerprints registered. Please go to Settings > Security to add one.", Toast.LENGTH_LONG).show()
                                }
                                else -> {
                                    Toast.makeText(context, "Biometric status unknown.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Disabling: also verify first so someone can't just turn it off
                            if (activity != null) {
                                val biometricManager = BiometricManager.from(context)
                                if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                                    val executor = ContextCompat.getMainExecutor(context)
                                    val biometricPrompt = BiometricPrompt(activity, executor,
                                        object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                super.onAuthenticationSucceeded(result)
                                                prefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, false).apply()
                                                isBiometricEnabled = false
                                                Toast.makeText(context, "Biometric login disabled.", Toast.LENGTH_SHORT).show()
                                            }
                                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
                                            }
                                            override fun onAuthenticationFailed() {
                                                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Confirm your fingerprint")
                                        .setSubtitle("Verify to disable biometric login")
                                        .setNegativeButtonText("Cancel")
                                        .build()
                                    biometricPrompt.authenticate(promptInfo)
                                } else {
                                    prefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, false).apply()
                                    isBiometricEnabled = false
                                }
                            }
                        }
                    }
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (isBiometricEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = "✓ App will require fingerprint verification every time you open it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
