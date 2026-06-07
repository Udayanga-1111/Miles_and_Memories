package com.example.milesmemories.ui.components

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Inline audio player for voice notes attached to a journey.
 */
@Composable
fun AudioPlayerRow(url: String, title: String) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isLoadingAudio by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(0) }
    var isDraggingSlider by remember { mutableStateOf(false) }

    DisposableEffect(url) {
        onDispose { mediaPlayer?.release() }
    }

    LaunchedEffect(isPlaying, isDraggingSlider) {
        while (isPlaying && !isDraggingSlider) {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    currentPosition = player.currentPosition
                }
            }
            delay(100)
        }
    }

    fun formatTime(ms: Int): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / 1000) / 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoadingAudio) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(onClick = {
                    if (isPlaying) {
                        mediaPlayer?.pause()
                        isPlaying = false
                    } else if (mediaPlayer == null) {
                        isLoadingAudio = true
                        try {
                            val streamUrl = if (url.contains("res.cloudinary.com") && !url.endsWith(".mp4")) {
                                if (url.contains(".")) url.substringBeforeLast(".") + ".mp4" else "$url.mp4"
                            } else {
                                url
                            }

                            val player = MediaPlayer()
                            player.setAudioAttributes(
                                android.media.AudioAttributes.Builder()
                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                    .build()
                            )
                            player.setDataSource(streamUrl)
                            player.prepareAsync()
                            player.setOnPreparedListener {
                                isLoadingAudio = false
                                totalDuration = it.duration
                                it.start()
                                isPlaying = true
                            }
                            player.setOnCompletionListener {
                                isPlaying = false
                                mediaPlayer?.seekTo(0)
                                currentPosition = 0
                            }
                            player.setOnErrorListener { _, what, extra ->
                                isLoadingAudio = false
                                Toast.makeText(
                                    context,
                                    "Cloudinary Audio Streaming Error: $what ($extra)",
                                    Toast.LENGTH_LONG
                                ).show()
                                false
                            }
                            mediaPlayer = player
                        } catch (e: Exception) {
                            isLoadingAudio = false
                            Toast.makeText(context, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        mediaPlayer?.start()
                        isPlaying = true
                    }
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause Audio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(visible = mediaPlayer != null) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = if (totalDuration > 0) currentPosition.toFloat() else 0f,
                        onValueChange = { newValue ->
                            isDraggingSlider = true
                            currentPosition = newValue.toInt()
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            mediaPlayer?.seekTo(currentPosition)
                        },
                        valueRange = 0f..(if (totalDuration > 0) totalDuration.toFloat() else 100f),
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(totalDuration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
