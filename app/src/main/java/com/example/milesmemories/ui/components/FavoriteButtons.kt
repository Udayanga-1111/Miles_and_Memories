package com.example.milesmemories.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.milesmemories.R

/**
 * Favorite toggle controls used on journey cards and note editor screens.
 */

@Composable
fun FavButton(
    noteId: String,
    isFavorite: Boolean,
    onToggle: (Boolean) -> Unit
) {
    var isFav by remember(noteId) { mutableStateOf(isFavorite) }

    LaunchedEffect(isFavorite) {
        isFav = isFavorite
    }

    Surface(
        onClick = {
            isFav = !isFav
            onToggle(isFav)
        },
        shape = CircleShape,
        color = if (isFav) {
            Color(0xFFFF4081).copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        },
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.nav_fav_icon),
                contentDescription = if (isFav) "Remove from favorites" else "Add to favorites",
                tint = if (isFav) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
fun FavoriteIconButton(
    isFavorite: Boolean,
    onToggle: (Boolean) -> Unit
) {
    var isFav by remember { mutableStateOf(isFavorite) }

    LaunchedEffect(isFavorite) {
        isFav = isFavorite
    }

    IconButton(
        onClick = {
            isFav = !isFav
            onToggle(isFav)
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.nav_fav_icon),
            contentDescription = if (isFav) "Remove from favorites" else "Add to favorites",
            tint = if (isFav) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurface
        )
    }
}
