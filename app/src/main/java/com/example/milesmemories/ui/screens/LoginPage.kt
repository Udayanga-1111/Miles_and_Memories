/**
 * Screen for user authentication (Login).
 * Supports Email/Password, Google Sign-In, and Biometric login.
 */
package com.example.milesmemories.ui.screens

import android.app.Activity
import com.example.milesmemories.ui.components.BiometricLockedScreen
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.milesmemories.R
import com.example.milesmemories.models.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginPage(
    navController: NavController
) {
    val verticalScroll: ScrollState = rememberScrollState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val auth = remember { FirebaseAuth.getInstance() }
    val prefs = remember { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }

    // Navigation logic
    val navigateToHome = {
        navController.navigate(Screen.HomePage.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun showBiometricPromptAndNavigate() {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS && activity != null) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        navigateToHome()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Stay on screen — user can retry
                        Toast.makeText(context, "Authentication cancelled", Toast.LENGTH_SHORT).show()
                    }
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(context, "Fingerprint not recognised. Try again.", Toast.LENGTH_SHORT).show()
                    }
                })
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Verify your fingerprint to continue")
                .setNegativeButtonText("Use password instead")
                .build()
            biometricPrompt.authenticate(promptInfo)
        } else {
            // Biometric unavailable — disable the setting and go home
            prefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, false).apply()
            navigateToHome()
        }
    }

    // Determine state: is user already logged in?
    val currentUser = auth.currentUser
    val biometricEnabled = prefs.getBoolean(PREF_BIOMETRIC_ENABLED, false)

    // Auto-login / biometric gate on composition
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            if (biometricEnabled) {
                showBiometricPromptAndNavigate()
            } else {
                navigateToHome()
            }
        }
    }

    // --- UI ---
    // If already logged in with biometrics, show a minimal "locked" screen
    if (currentUser != null && biometricEnabled) {
        BiometricLockedScreen(
            onUnlock = { showBiometricPromptAndNavigate() },
            onSignOut = {
                auth.signOut()
                prefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, false).apply()
                // Re-compose will trigger with currentUser == null → show login form
            }
        )
        return
    }

    // Fixed GSO block using stringResource
    val webClientId = stringResource(id = R.string.default_web_client_id)
    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
    }
    
    val googleSignInClient = remember(context, gso) { 
        GoogleSignIn.getClient(context, gso) 
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginPage", "Google Sign In Result Code: ${result.resultCode}")
        
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                        isLoading = false
                        if (authTask.isSuccessful) {
                            Log.d("LoginPage", "Firebase Google Auth Successful")
                            Toast.makeText(context, "Google Login Successful", Toast.LENGTH_SHORT).show()
                            navigateToHome()
                        } else {
                            Log.e("LoginPage", "Firebase Google Auth Failed", authTask.exception)
                            Toast.makeText(context, "Auth Failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    isLoading = false
                    Toast.makeText(context, "Google sign-in failed: No ID Token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isLoading = false
                val message = if (e is ApiException) "Error code: ${e.statusCode}" else e.message
                Toast.makeText(context, "Google error: $message", Toast.LENGTH_SHORT).show()
            }
        } else {
            isLoading = false
            if (result.resultCode != Activity.RESULT_CANCELED) {
                Toast.makeText(context, "Google sign-in failed. Code: ${result.resultCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .padding(32.dp)
                .verticalScroll(verticalScroll),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(75.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(elevation = 8.dp)
            )
            
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") },
                modifier = Modifier.width(400.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Icon") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.width(400.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                    navigateToHome()
                                } else {
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.width(400.dp).height(50.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Login", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("or", modifier = Modifier.padding(horizontal = 16.dp))
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { 
                    if (!isLoading) {
                        isLoading = true
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(24.dp),
                enabled = !isLoading
            ) {
                Text("Google", color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ")
                TextButton(onClick = { navController.navigate(Screen.SignupPage.route) }) {
                    Text("Sign Up", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

