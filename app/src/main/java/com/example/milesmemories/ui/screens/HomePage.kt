package com.example.milesmemories.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.milesmemories.ui.components.FAB
import com.example.milesmemories.ui.components.Header
import com.example.milesmemories.ui.components.NavigationBar
import com.example.milesmemories.ui.components.DynamicLandscapeCard
import com.example.milesmemories.ui.components.DynamicPortraitCard
import com.example.milesmemories.ui.components.SearchBar
import com.example.milesmemories.models.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import com.example.milesmemories.ui.components.TitleHeader

@Composable
fun HomePage(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val notes = remember { mutableStateListOf<Note>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("notes")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("HomePage", "Listen failed.", error)
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        notes.clear()
                        for (doc in snapshot.documents) {
                            val note = doc.toObject(Note::class.java)
                            if (note != null) notes.add(note)
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
            TitleHeader(
                searchBar = true,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
        },
        bottomBar = {
            NavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_note_page/New Note?title=&description=&date=Select Date")},
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                FAB()
            }
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(innerPadding)
        ) {

            // Main Content
            LazyColumn(
                modifier = Modifier
                    .padding(
                        horizontal = if (isLandscape) 40.dp else 15.dp
                    )
                    .weight(1f)
            ) {
                item {
                    if (isLandscape) {
                        Header("Journeys", "${notes.size}")
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Header("Journeys", "${notes.size}")
                        Spacer(modifier = Modifier.height(10.dp))
                        SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                val filteredNotes = if (searchQuery.isBlank()) notes else notes.filter { 
                    it.title.contains(searchQuery, ignoreCase = true) || 
                    it.content.contains(searchQuery, ignoreCase = true) 
                }

                if (isLoading) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(20.dp))
                    }
                } else if (notes.isEmpty()) {
                    item {
                        androidx.compose.material3.Text(
                            text = "No journeys yet. Tap the + button to add one!",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else if (filteredNotes.isEmpty()) {
                    item {
                        androidx.compose.material3.Text(
                            text = "No journeys match your search.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    filteredNotes.forEach { note ->
                        item {
                            val dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.date))
                            val coverImageUrl = if (note.imageUrls.isNotEmpty()) note.imageUrls.first() else null
                            val navRoute = "note_details_page/${note.id}"
                            
                            if (isLandscape) {
                                DynamicLandscapeCard(
                                    title = note.title,
                                    description = note.content,
                                    date = dateString,
                                    coverImage = coverImageUrl,
                                    isFavorite = note.isFavorite,
                                    onFavToggle = { isFav ->
                                        FirebaseFirestore.getInstance().collection("notes")
                                            .document(note.id)
                                            .update("isFavorite", isFav)
                                            .addOnFailureListener { e ->
                                                Log.e("HomePage", "Error updating favorite", e)
                                            }
                                    },
                                    onClick = { navController.navigate(navRoute) }
                                )
                            }else{
                                DynamicPortraitCard(
                                    title = note.title,
                                    description = note.content,
                                    date = dateString,
                                    coverImage = coverImageUrl,
                                    isFavorite = note.isFavorite,
                                    onFavToggle = { isFav ->
                                        FirebaseFirestore.getInstance().collection("notes")
                                            .document(note.id)
                                            .update("isFavorite", isFav)
                                            .addOnFailureListener { e ->
                                                Log.e("HomePage", "Error updating favorite", e)
                                            }
                                    },
                                    onClick = { navController.navigate(navRoute) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
