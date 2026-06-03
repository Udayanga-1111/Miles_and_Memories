package com.example.milesmemories.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.milesmemories.ui.components.DynamicAlbumCard
import com.example.milesmemories.ui.components.Header
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.milesmemories.models.Album
import androidx.compose.material3.CircularProgressIndicator
import android.util.Log
import androidx.compose.foundation.layout.Row
import com.example.milesmemories.ui.components.NavigationBar
import com.example.milesmemories.ui.components.TitleHeader

@Composable
fun AlbumPage(navController: NavController){

    val verticalScroll: ScrollState = rememberScrollState()
    
    val albums = remember { mutableStateListOf<Album>() }
    var isLoading by remember { mutableStateOf(true) }
    var selectedAlbums by remember { mutableStateOf(setOf<Album>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("albums")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AlbumPage", "Listen failed.", error)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        albums.clear()
                        for (doc in snapshot.documents) {
                            val album = doc.toObject(Album::class.java)
                            if (album != null) albums.add(album)
                        }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TitleHeader(false)
        },
        bottomBar = {
            NavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .verticalScroll(verticalScroll)
                .padding(innerPadding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(modifier = Modifier.weight(1f)) {
                    Header("Album")
                }
                
                if (selectedAlbums.isNotEmpty()) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Main Content
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(20.dp))
            } else if (albums.isEmpty()) {
                androidx.compose.material3.Text(
                    text = "No albums yet. Add notes with images to see them here!",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 10.dp)
                ) {
                    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    val columns = if (isLandscape) 3 else 2
                    val spacing = 15.dp
                    val itemWidth = (maxWidth - spacing * (columns - 1)) / columns

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.Start),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        albums.forEach { album ->
                            val isSelected = selectedAlbums.contains(album)
                            DynamicAlbumCard(
                                navController = navController,
                                albumId = album.id,
                                title = album.title,
                                imageUrls = album.imageUrls,
                                isSelected = isSelected,
                                modifier = Modifier.width(itemWidth),
                                onClick = {
                                    if (selectedAlbums.isNotEmpty()) {
                                        val newSelection = selectedAlbums.toMutableSet()
                                        if (isSelected) newSelection.remove(album) else newSelection.add(album)
                                        selectedAlbums = newSelection
                                    } else {
                                        navController.navigate(route = "picture_page/${album.id}")
                                    }
                                },
                                onLongClick = {
                                    val newSelection = selectedAlbums.toMutableSet()
                                    if (isSelected) newSelection.remove(album) else newSelection.add(album)
                                    selectedAlbums = newSelection
                                }
                            )
                        }
                    }
                }
            }

            if (showDeleteConfirm) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { androidx.compose.material3.Text("Delete Albums") },
                    text = { androidx.compose.material3.Text("Are you sure you want to delete ${selectedAlbums.size} album(s)? This will also remove the images from the associated notes.") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                selectedAlbums.forEach { album ->
                                    FirebaseFirestore.getInstance().collection("notes").document(album.noteId)
                                        .update("imageUrls", emptyList<String>())
                                    FirebaseFirestore.getInstance().collection("albums").document(album.id).delete()
                                }
                                selectedAlbums = emptySet()
                                showDeleteConfirm = false
                            }
                        ) {
                            androidx.compose.material3.Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                            androidx.compose.material3.Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
