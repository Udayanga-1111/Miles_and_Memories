package com.example.milesmemories.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPage(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var isBiometricEnabled by remember { mutableStateOf(false) }

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
                            text = "Use fingerprint to authenticate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            val biometricManager = BiometricManager.from(context)
                            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                                BiometricManager.BIOMETRIC_SUCCESS -> {
                                    if (activity != null) {
                                        val executor = ContextCompat.getMainExecutor(context)
                                        val biometricPrompt = BiometricPrompt(activity, executor,
                                            object : BiometricPrompt.AuthenticationCallback() {
                                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                    super.onAuthenticationError(errorCode, errString)
                                                    Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                                                    isBiometricEnabled = false
                                                }
                                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                    super.onAuthenticationSucceeded(result)
                                                    Toast.makeText(context, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                                                    isBiometricEnabled = true
                                                }
                                                override fun onAuthenticationFailed() {
                                                    super.onAuthenticationFailed()
                                                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                                                    isBiometricEnabled = false
                                                }
                                            })
                                            
                                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                            .setTitle("Biometric login")
                                            .setSubtitle("Log in using your biometric credential")
                                            .setNegativeButtonText("Cancel")
                                            .build()
                                            
                                        biometricPrompt.authenticate(promptInfo)
                                    } else {
                                        Toast.makeText(context, "Cannot show biometric prompt", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                                    Toast.makeText(context, "No biometric features available on this device.", Toast.LENGTH_LONG).show()
                                }
                                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                                    Toast.makeText(context, "Biometric features are currently unavailable.", Toast.LENGTH_LONG).show()
                                }
                                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                    Toast.makeText(context, "No fingerprints registered on device. Please register one in settings.", Toast.LENGTH_LONG).show()
                                }
                                else -> {
                                    Toast.makeText(context, "Biometric status unknown.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            isBiometricEnabled = false
                        }
                    }
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
