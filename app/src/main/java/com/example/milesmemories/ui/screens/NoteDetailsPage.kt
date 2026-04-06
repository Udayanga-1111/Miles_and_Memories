package com.example.milesmemories.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.milesmemories.R
import com.example.milesmemories.models.Note
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsPage(
    noteId: String,
    onNavigateBack: () -> Unit = {},
    navController: NavController
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    var note by remember { mutableStateOf<Note?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(noteId) {
        FirebaseFirestore.getInstance().collection("notes").document(noteId)
            .get()
            .addOnSuccessListener { document ->
                note = document.toObject(Note::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isLoading) {
        if (!isLoading) visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    note?.let { n ->
                        val dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(n.date))
                        val safeTitle = java.net.URLEncoder.encode(n.title.ifEmpty { " " }, "UTF-8")
                        val safeDesc = java.net.URLEncoder.encode(n.content.ifEmpty { " " }, "UTF-8")
                        val safeDate = java.net.URLEncoder.encode(dateString, "UTF-8")
                        IconButton(onClick = { navController.navigate("add_note_page/Edit Note?title=$safeTitle&description=$safeDesc&date=$safeDate") }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = {
                        note?.id?.let { id ->
                            FirebaseFirestore.getInstance().collection("notes").document(id).delete()
                                .addOnSuccessListener { 
                                    onNavigateBack()
                                }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (note == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Note not found")
            }
        } else {
            val currentNote = note!!
            val dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(currentNote.date))
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title And The Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    if (currentNote.imageUrls.isNotEmpty()) {
                        AsyncImage(
                            model = currentNote.imageUrls.first(),
                            contentDescription = "Cover Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.cardexample),
                            contentDescription = "Cover Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.surface
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )

                    ) { }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = if (isLandscape) 40.dp else 16.dp
                            )
                            .align(Alignment.BottomStart)
                    ) {
                        // Date
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Date",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        Text(
                            text = currentNote.title,
                            fontSize = 40.sp,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 1000)
                    ) + slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 4 },
                        animationSpec = tween(durationMillis = 1000)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = if (isLandscape) 40.dp else 16.dp
                        )
                ) {
                    Column {
                        Text(
                            text = "Text Notes",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        // Description
                        Text(
                            text = currentNote.content,
                            textAlign = TextAlign.Justify,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Voice Records Section
                if (currentNote.voiceUrls.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isLandscape) 40.dp else 16.dp)
                    ) {
                        Text(
                            text = "Voice Records",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        currentNote.voiceUrls.forEachIndexed { index, url ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Audio",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Journey Audio Note ${index + 1}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Image Section
                if (currentNote.imageUrls.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isLandscape) 40.dp else 16.dp)
                    ) {
                        Text(
                            text = "Images",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentNote.imageUrls.forEach { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
