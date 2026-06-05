package com.example.milesmemories.models

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName

data class Note(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val date: Long = 0,
    val location: String = "",
    val imageUrls: List<String> = emptyList(),
    val voiceUrls: List<String> = emptyList(),
    val voiceNames: List<String> = emptyList(),
    @get:PropertyName("isFavorite")
    @set:PropertyName("isFavorite")
    @PropertyName("isFavorite")
    var isFavorite: Boolean = false
)

fun DocumentSnapshot.toNote(): Note? =
    toObject(Note::class.java)?.copy(id = id)
