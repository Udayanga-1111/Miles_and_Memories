package com.example.milesmemories.models

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
    val isFavorite: Boolean = false
)
