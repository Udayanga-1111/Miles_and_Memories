package com.example.milesmemories.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "FavoriteUtils"

fun updateNoteFavorite(
    noteId: String,
    isFavorite: Boolean,
    onFailure: (() -> Unit)? = null
) {
    if (noteId.isBlank()) return
    FirebaseFirestore.getInstance()
        .collection("notes")
        .document(noteId)
        .update("isFavorite", isFavorite)
        .addOnFailureListener { e ->
            Log.e(TAG, "Failed to update favorite for $noteId", e)
            onFailure?.invoke()
        }
}
