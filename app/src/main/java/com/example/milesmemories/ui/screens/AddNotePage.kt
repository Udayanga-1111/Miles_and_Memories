package com.example.milesmemories.ui.screens

import android.Manifest
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
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
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.milesmemories.models.Note
import com.example.milesmemories.models.Album
import androidx.compose.material3.CircularProgressIndicator
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.firebase.storage.StorageReference
import android.util.Log

@Composable
fun AddNotePage(
    navController: NavController,
    page: String,
    title: String?,
    description: String?,
    date: String?
) {
    var noteTitle by remember {
        mutableStateOf(title ?: "")
    }

    var noteContent by remember {
        mutableStateOf(description ?: "")
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    var selectedDateMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var dateString = date ?: remember(selectedDateMillis) {
        selectedDateMillis?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
        } ?: "Select Date"
    }

    val selectedImages = remember { mutableStateListOf<Uri>() }
    val selectedAudio = remember { mutableStateListOf<Uri>() }
    
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentAudioFile by remember { mutableStateOf<File?>(null) }
    var fullScreenImageUri by remember { mutableStateOf<Uri?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }

    // Image picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            selectedImages.addAll(uris)
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
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch(e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        isRecording = false
        currentAudioFile?.let {
            selectedAudio.add(Uri.fromFile(it))
        }
        currentAudioFile = null
    }

    suspend fun uploadFileSafely(ref: StorageReference, uri: Uri): String = suspendCoroutine { continuation ->
        Log.d("AddNotePage", "Uploading: $uri to ${ref.path}")
        ref.putFile(uri)
            .addOnSuccessListener {
                Log.d("AddNotePage", "Upload success: ${ref.path}")
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    Log.d("AddNotePage", "Download URL: $downloadUri")
                    continuation.resume(downloadUri.toString())
                }.addOnFailureListener { e ->
                    Log.e("AddNotePage", "Failed to get download URL", e)
                    continuation.resumeWithException(Exception("Failed to get download URL: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddNotePage", "Upload failed", e)
                continuation.resumeWithException(Exception("Upload failed on Storage: ${e.message}"))
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
                    val ref = storage.reference.child("images/${currentUser.uid}/${System.currentTimeMillis()}_${uri.lastPathSegment}")
                    val url = uploadFileSafely(ref, uri)
                    uploadedImageUrls.add(url)
                }

                // 2. Upload Audio
                val uploadedAudioUrls = mutableListOf<String>()
                for (uri in selectedAudio) {
                    val ref = storage.reference.child("audio/${currentUser.uid}/${System.currentTimeMillis()}_${uri.lastPathSegment}")
                    val url = uploadFileSafely(ref, uri)
                    uploadedAudioUrls.add(url)
                }

                // 3. Save Note
                val noteRef = db.collection("notes").document()
                val newNote = Note(
                    id = noteRef.id,
                    userId = currentUser.uid,
                    title = noteTitle,
                    content = noteContent,
                    date = selectedDateMillis ?: System.currentTimeMillis(),
                    imageUrls = uploadedImageUrls,
                    voiceUrls = uploadedAudioUrls
                )
                noteRef.set(newNote).await()

                // 4. Create Album if images exist
                if (uploadedImageUrls.isNotEmpty()) {
                    val albumRef = db.collection("albums").document()
                    val newAlbum = Album(
                        id = albumRef.id,
                        userId = currentUser.uid,
                        noteId = noteRef.id,
                        title = noteTitle,
                        imageUrls = uploadedImageUrls
                    )
                    albumRef.set(newAlbum).await()
                }

                isSaving = false
                Toast.makeText(context, "Note successfully saved!", Toast.LENGTH_SHORT).show()
                navController.navigate("home_screen") { popUpTo(0) { inclusive = true } }
            } catch (e: Exception) {
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
                    Discard(navController)
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
            // Title Input
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

            // Date Picker
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
                if (selectedAudio.isNotEmpty()) {
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
                        selectedAudio.forEach { uri ->
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
                                    contentDescription = "Audio Item",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Voice Record added",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Audio",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { selectedAudio.remove(uri) }
                                )
                            }
                        }
                    }
                }

                // Selected Images list
                if (selectedImages.isNotEmpty()) {
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
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
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
}

@Composable
fun Discard(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    Text(
        text = "Discard",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .padding(8.dp)
            .clickable { showDialog = true }
    )

    ConfirmDiscard(
        showDialog,
        onDismiss = { showDialog = false },
        onConfirm = {
            showDialog = false
            navController.navigate("home_screen")
        }
    )
}

@Composable
fun ConfirmDiscard(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Confirm Discard") },
            text = { Text("Are you sure you want to Discard?") },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(
                        "Discard",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    }
}
