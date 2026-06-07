package com.example.milesmemories.models

data class Album(
    val id: String = "",
    val userId: String = "",
    val noteId: String = "",
    val title: String = "",
    val imageUrls: List<String> = emptyList()
)
