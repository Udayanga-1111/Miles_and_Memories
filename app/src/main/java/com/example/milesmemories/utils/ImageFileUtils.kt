package com.example.milesmemories.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

/**
 * File helpers for camera capture workflows on the Add Note screen.
 */
object ImageFileUtils {

    fun createTempImageUri(context: Context): Uri {
        val tempFile = File.createTempFile(
            "camera_img_",
            ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }
}
