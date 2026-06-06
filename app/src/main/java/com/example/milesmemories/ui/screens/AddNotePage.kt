/**
 * Screen for adding or editing a note/journey.
 * Handles text input, location picking, image selection, and voice recording.
 */
package com.example.milesmemories.ui.screens

import android.Manifest
import com.example.milesmemories.ui.components.DiscardButton
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import androidx.core.content.ContextCompat
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import android.content.Intent
import android.content.ActivityNotFoundException
import com.google.android.gms.location.LocationServices
import androidx.compose.runtime.collectAsState
import com.example.milesmemories.utils.SharedLocationManager
import android.annotation.SuppressLint
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.milesmemories.ui.components.DatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.milesmemories.models.Note
import com.example.milesmemories.models.toNote
import com.example.milesmemories.ui.components.FavoriteIconButton
import com.example.milesmemories.utils.updateNoteFavorite
import com.example.milesmemories.models.Album
import androidx.compose.material3.CircularProgressIndicator
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import org.osmdroid.config.Configuration as OsmConfig
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import android.os.Environment

@Composable
fun AddNotePage(
    navController: NavController,
    page: String,
    noteId: String?
) {
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showMapPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    var selectedDateMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var dateString = remember(selectedDateMillis) {
        selectedDateMillis?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
        } ?: "Select Date"
    }

    val existingImageUrls = remember { mutableStateListOf<String>() }
    val existingAudioUrls = remember { mutableStateListOf<String>() }
    val existingAudioNames = remember { mutableStateListOf<String>() }
    var originalNote by remember(noteId) { mutableStateOf<Note?>(null) }
    var isFavorite by remember(noteId) { mutableStateOf(false) }

    val selectedImages = remember { mutableStateListOf<Uri>() }
    val selectedAudio = remember { mutableStateListOf<Uri>() }
    val selectedAudioNames = remember { mutableStateListOf<String>() }
    
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentAudioFile by remember { mutableStateOf<File?>(null) }
    var fullScreenImageUri by remember { mutableStateOf<Uri?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    
    // Camera Support
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(noteId) {
        if (!noteId.isNullOrEmpty()) {
            db.collection("notes").document(noteId)
                .addSnapshotListener { document, error ->
                    if (error == null && document != null && document.exists()) {
                        val n = document.toNote() ?: return@addSnapshotListener
                        isFavorite = n.isFavorite
                        if (originalNote == null) {
                            originalNote = n
                            noteTitle = n.title
                            noteContent = n.content
                            location = n.location
                            selectedDateMillis = n.date

                            existingImageUrls.clear()
                            existingImageUrls.addAll(n.imageUrls)
                            existingAudioUrls.clear()
                            existingAudioUrls.addAll(n.voiceUrls)
                            existingAudioNames.clear()

                            val names = n.voiceNames.toMutableList()
                            while (names.size < n.voiceUrls.size) {
                                names.add("Journey Audio Note ${names.size + 1}")
                            }
                            existingAudioNames.addAll(names)
                        }
                    }
                }
        }
    }

    // Shared Location Observer
    val pendingLoc by SharedLocationManager.pendingLocation.collectAsState()
    LaunchedEffect(pendingLoc) {
        if (pendingLoc != null) {
            location = pendingLoc!!
            SharedLocationManager.pendingLocation.value = null
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                        val addressName = if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val feature = addr.featureName
                            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                            val isNumeric = feature?.matches(Regex("\\d+[a-zA-Z]*(-?\\d+[a-zA-Z]*)?")) == true
                            if (!feature.isNullOrBlank() && !isNumeric && feature != city) {
                                if (!city.isNullOrBlank()) "$feature, $city" else feature
                            } else if (!city.isNullOrBlank()) {
                                city
                            } else {
                                addr.getAddressLine(0) ?: "${loc.latitude},${loc.longitude}"
                            }
                        } else {
                            "${loc.latitude},${loc.longitude}"
                        }
                        withContext(Dispatchers.Main) {
                            location = addressName
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            location = "${loc.latitude},${loc.longitude}"
                            Toast.makeText(context, "Could not get address name.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Location not found. Ensure location is enabled.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to get location.", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted || coarseLocationGranted) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Image picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            selectedImages.addAll(uris)
        }
    )

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                capturedImageUri?.let { selectedImages.add(it) }
            }
        }
    )

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createTempImageUri(context)
                capturedImageUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permission Denied. Cannot record.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun startRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.3gp")
        val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder.setOutputFile(file.absolutePath)

        try {
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            currentAudioFile = file
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start recording.", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        var recordingSuccessful = true
        try {
            mediaRecorder?.stop()
        } catch(e: Exception) {
            e.printStackTrace()
            recordingSuccessful = false
            Toast.makeText(context, "Recording was too short to save.", Toast.LENGTH_SHORT).show()
        } finally {
            try {
                mediaRecorder?.release()
            } catch (e: Exception) {}
        }
        
        mediaRecorder = null
        isRecording = false
        
        if (recordingSuccessful) {
            currentAudioFile?.let {
                if (it.exists() && it.length() > 0) {
                    val newUri = Uri.fromFile(it)
                    selectedAudio.add(newUri)
                    selectedAudioNames.add("New Voice Record")
                } else {
                    Toast.makeText(context, "Failed to save audio file.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            currentAudioFile?.delete()
        }
        currentAudioFile = null
    }

    suspend fun uploadToCloudinarySafely(uri: Uri, isAudio: Boolean): String = suspendCoroutine { continuation ->
        try {
            val resourceType = if (isAudio) "video" else "image"
            
            MediaManager.get().upload(uri)
                .option("resource_type", resourceType) // Important for .3gp audio
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("AddNotePage", "Cloudinary upload started: $uri")
                    }
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as String?
                        if (secureUrl != null) {
                            Log.d("AddNotePage", "Cloudinary upload success: $secureUrl")
                            continuation.resume(secureUrl)
                        } else {
                            continuation.resumeWithException(Exception("Empty secure_url returned"))
                        }
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("AddNotePage", "Cloudinary Upload failed: ${error.description}")
                        continuation.resumeWithException(Exception("Cloudinary Error: ${error.description}"))
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        } catch (e: Exception) {
            Log.e("AddNotePage", "Cloudinary local setup error", e)
            continuation.resumeWithException(Exception("Could not start upload: ${e.message}"))
        }
    }

    fun saveNote() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "You must be logged in to save.", Toast.LENGTH_SHORT).show()
            return
        }
        if (noteTitle.isBlank() || noteContent.isBlank()) {
            Toast.makeText(context, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true
        coroutineScope.launch {
            try {
                // 1. Upload Images
                val uploadedImageUrls = mutableListOf<String>()
                for (uri in selectedImages) {
                    val url = uploadToCloudinarySafely(uri, false)
                    uploadedImageUrls.add(url)
                }

                // 2. Upload Audio
                val uploadedAudioUrls = mutableListOf<String>()
                for (uri in selectedAudio) {
                    val url = uploadToCloudinarySafely(uri, true)
                    uploadedAudioUrls.add(url)
                }

                // 3. Save Note
                val noteRef = if (noteId.isNullOrEmpty()) {
                    db.collection("notes").document()
                } else {
                    db.collection("notes").document(noteId)
                }

                val finalImageUrls = existingImageUrls + uploadedImageUrls
                val finalVoiceUrls = existingAudioUrls + uploadedAudioUrls
                val finalVoiceNames = existingAudioNames + selectedAudioNames
                
                val newNote = Note(
                    id = noteRef.id,
                    userId = currentUser.uid,
                    title = noteTitle,
                    content = noteContent,
                    location = location,
                    date = selectedDateMillis ?: System.currentTimeMillis(),
                    imageUrls = finalImageUrls,
                    voiceUrls = finalVoiceUrls,
                    voiceNames = finalVoiceNames,
                    isFavorite = isFavorite
                )
                noteRef.set(newNote) // Overwrites note while keeping favorite status and id intact

                // 4. Create or Update Album if images exist
                if (finalImageUrls.isNotEmpty()) {
                    val albumSnapshot = db.collection("albums").whereEqualTo("noteId", noteRef.id).get().await()
                    if (!albumSnapshot.isEmpty) {
                        val albumDoc = albumSnapshot.documents[0]
                        db.collection("albums").document(albumDoc.id).update(
                            mapOf(
                                "title" to noteTitle,
                                "imageUrls" to finalImageUrls
                            )
                        )
                    } else {
                        val albumRef = db.collection("albums").document()
                        val newAlbum = Album(
                            id = albumRef.id,
                            userId = currentUser.uid,
                            noteId = noteRef.id,
                            title = noteTitle,
                            imageUrls = finalImageUrls
                        )
                        albumRef.set(newAlbum)
                    }
                } else {
                    val albumSnapshot = db.collection("albums").whereEqualTo("noteId", noteRef.id).get().await()
                    for (doc in albumSnapshot.documents) {
                        db.collection("albums").document(doc.id).delete()
                    }
                }

                isSaving = false
                Toast.makeText(context, "Note successfully saved!", Toast.LENGTH_SHORT).show()
                navController.navigate("home_screen") { popUpTo(0) { inclusive = true } }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                isSaving = false
                Log.e("AddNotePage", "Error in saveNote", e)
                Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = page,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FavoriteIconButton(
                        isFavorite = isFavorite,
                        onToggle = { isFav ->
                            isFavorite = isFav
                            if (!noteId.isNullOrEmpty()) {
                                updateNoteFavorite(noteId, isFav) {
                                    isFavorite = !isFav
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DiscardButton(navController)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                        )
                    } else {
                        IconButton(onClick = { saveNote() }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
            TextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                placeholder = {
                    Text(
                        "Title",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (showDatePicker) {
                DatePicker(
                    onDateSelected = {
                        selectedDateMillis = it
                        dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it!!))
                        showDatePicker = false},
                    onDismiss = { showDatePicker = false }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (location.isNotBlank()) {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showMapPicker = true
                            }
                            .padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = "Location: Not Set",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    )
                }
                
                // Fetch Location Button
                IconButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fetchCurrentLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Get Current Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // Open Maps Button
                IconButton(onClick = { showMapPicker = true }) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Open in Maps",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note Content Input
            TextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                placeholder = {
                    Text(
                        "Start typing your note...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = screenHeight * 0.6f)
            )

            // Attached Items Lists
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Selected Audio list
                if (selectedAudio.isNotEmpty() || existingAudioUrls.isNotEmpty()) {
                    Text(
                        text = "Voice Records",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        existingAudioUrls.forEachIndexed { index, url ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Existing Audio Item",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextField(
                                    value = existingAudioNames[index],
                                    onValueChange = { existingAudioNames[index] = it },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Audio",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { 
                                            existingAudioUrls.removeAt(index)
                                            existingAudioNames.removeAt(index)
                                        }
                                )
                            }
                        }

                        selectedAudio.forEachIndexed { index, uri ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "New Audio Item",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextField(
                                    value = selectedAudioNames[index],
                                    onValueChange = { selectedAudioNames[index] = it },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Audio",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { 
                                            selectedAudio.removeAt(index)
                                            selectedAudioNames.removeAt(index)
                                        }
                                )
                            }
                        }
                    }
                }

                // Selected Images list
                if (selectedImages.isNotEmpty() || existingImageUrls.isNotEmpty()) {
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        items(existingImageUrls) { url ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Existing Image",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            RoundedCornerShape(50)
                                        )
                                        .padding(2.dp)
                                        .clickable { existingImageUrls.remove(url) }
                                )
                            }
                        }

                        items(selectedImages) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { fullScreenImageUri = uri }
                                )
                                // Remove button for image
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Image",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            RoundedCornerShape(50)
                                        )
                                        .padding(2.dp)
                                        .clickable { selectedImages.remove(uri) }
                                )
                            }
                        }
                    }
                }
            }
            } // End of scrollable Column

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer)
                        .clickable {
                            if (isRecording) {
                                stopRecording()
                            } else {
                                startRecording()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record Voice",
                        tint = if (isRecording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "Stop" else "Voice Note",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isRecording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Image Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { showImageSourceDialog = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add Image",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Image",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Select Image Source") },
            text = { Text("How would you like to add an image?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val uri = createTempImageUri(context)
                        capturedImageUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text("Take Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Select from Gallery")
                }
            }
        )
    }

    if (fullScreenImageUri != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = fullScreenImageUri,
                    contentDescription = "Full Screen Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { fullScreenImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Full Screen",
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (showMapPicker) {
        var mapMarkerLocation by remember { mutableStateOf<GeoPoint?>(null) }
        
        Dialog(
            onDismissRequest = { showMapPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Bar
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
                        IconButton(onClick = { showMapPicker = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Search Bar
                    var mapSearchQuery by remember { mutableStateOf("") }
                    var isSearchingLocation by remember { mutableStateOf(false) }
                    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
                    var mapMarkerRef by remember { mutableStateOf<Marker?>(null) }

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
                            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                if (mapSearchQuery.isNotBlank()) {
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
                            }),
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
                            onClick = {
                                if (mapSearchQuery.isNotBlank()) {
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
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
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

                    // MapView
                    AndroidView(
                        factory = { ctx ->
                            OsmConfig.getInstance().userAgentValue = ctx.packageName
                            val mapView = MapView(ctx)
                            mapView.setMultiTouchControls(true)
                            mapView.controller.setZoom(15.0)

                            // Initial position (use current location if set, else center of world)
                            val startPoint = if (location.isNotBlank() && location.contains(",")) {
                                val parts = location.split(",")
                                val lat = parts[0].trim().toDoubleOrNull()
                                val lng = parts[1].trim().toDoubleOrNull()
                                if (lat != null && lng != null) {
                                    GeoPoint(lat, lng)
                                } else {
                                    GeoPoint(6.9271, 79.8612) // Default to Colombo
                                }
                            } else {
                                GeoPoint(6.9271, 79.8612)
                            }
                            mapView.controller.setCenter(startPoint)

                            val marker = Marker(mapView)
                            marker.position = startPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(marker)
                            mapMarkerLocation = startPoint
                            mapMarkerRef = marker
                            mapViewRef = mapView

                            val receive = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                    p?.let {
                                        marker.position = it
                                        mapMarkerLocation = it
                                        mapView.invalidate()
                                    }
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint?): Boolean = false
                            }
                            mapView.overlays.add(MapEventsOverlay(receive))

                            mapView
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Confirm Button
                    TextButton(
                        onClick = {
                            mapMarkerLocation?.let { geoPoint ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val geocoder = Geocoder(context, Locale.getDefault())
                                        val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                                        val addressName = if (!addresses.isNullOrEmpty()) {
                                            val addr = addresses[0]
                                            val feature = addr.featureName
                                            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                                            val isNumeric = feature?.matches(Regex("\\d+[a-zA-Z]*(-?\\d+[a-zA-Z]*)?")) == true
                                            if (!feature.isNullOrBlank() && !isNumeric && feature != city) {
                                                if (!city.isNullOrBlank()) "$feature, $city" else feature
                                            } else if (!city.isNullOrBlank()) {
                                                city
                                            } else {
                                                addr.getAddressLine(0) ?: "${geoPoint.latitude},${geoPoint.longitude}"
                                            }
                                        } else {
                                            "${geoPoint.latitude},${geoPoint.longitude}"
                                        }
                                        withContext(Dispatchers.Main) {
                                            location = addressName
                                            showMapPicker = false
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            location = "${geoPoint.latitude},${geoPoint.longitude}"
                                            showMapPicker = false
                                        }
                                    }
                                }
                            } ?: run {
                                showMapPicker = false
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
}

fun createTempImageUri(context: android.content.Context): Uri {
    val tempFile = File.createTempFile("camera_img_", ".jpg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES))
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}

