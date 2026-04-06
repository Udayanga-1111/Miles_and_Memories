package com.example.milesmemories.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.milesmemories.ui.components.NavigationBar
import com.example.milesmemories.ui.components.TitleHeader

@Composable
fun AlbumPage(navController: NavController){

    val verticalScroll: ScrollState = rememberScrollState()
    
    val albums = remember { mutableStateListOf<Album>() }
    var isLoading by remember { mutableStateOf(true) }

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

            Box(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp)
            ){
                Header("Album")
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
                FlowRow(
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    albums.forEach { album ->
                        DynamicAlbumCard(
                            navController = navController,
                            albumId = album.id,
                            title = album.title,
                            imageUrls = album.imageUrls
                        )
                    }
                }
            }
        }
    }
}
