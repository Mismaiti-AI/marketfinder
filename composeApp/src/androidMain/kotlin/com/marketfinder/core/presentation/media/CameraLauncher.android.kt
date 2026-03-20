package com.marketfinder.core.presentation.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.marketfinder.core.data.media.MediaResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun rememberCameraLauncher(
    mode: CameraCaptureMode,
    onResult: (MediaResult?) -> Unit
): CameraLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return when (mode) {
        CameraCaptureMode.PHOTO -> rememberPhotoCameraLauncher(context, scope, onResult)
        CameraCaptureMode.VIDEO -> rememberVideoCameraLauncher(context, scope, onResult)
        CameraCaptureMode.PHOTO_AND_VIDEO -> rememberPhotoAndVideoCameraLauncher(context, scope, onResult)
    }
}

@Composable
private fun rememberPhotoCameraLauncher(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (MediaResult?) -> Unit
): CameraLauncher {
    val photoUri = remember { createTempUri(context, "camera_photo", ".jpg") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            scope.launch {
                val result = readMediaResult(context, photoUri, "camera_photo.jpg", "image/jpeg")
                onResult(result)
            }
        } else {
            onResult(null)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(photoUri)
        } else {
            onResult(null)
        }
    }

    return remember(cameraLauncher, permissionLauncher) {
        object : CameraLauncher {
            override fun launch() {
                if (hasCameraPermission(context)) {
                    cameraLauncher.launch(photoUri)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

@Composable
private fun rememberVideoCameraLauncher(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (MediaResult?) -> Unit
): CameraLauncher {
    val videoUri = remember { createTempUri(context, "camera_video", ".mp4") }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            scope.launch {
                val result = readVideoResult(context, videoUri)
                onResult(result)
            }
        } else {
            onResult(null)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            videoLauncher.launch(videoUri)
        } else {
            onResult(null)
        }
    }

    return remember(videoLauncher, permissionLauncher) {
        object : CameraLauncher {
            override fun launch() {
                if (hasCameraPermission(context)) {
                    videoLauncher.launch(videoUri)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

@Composable
private fun rememberPhotoAndVideoCameraLauncher(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (MediaResult?) -> Unit
): CameraLauncher {
    // Android doesn't have a single contract for photo+video choice,
    // so we use an intent-based approach with ACTION_IMAGE_CAPTURE + ACTION_VIDEO_CAPTURE chooser.
    // Simplest approach: use photo capture as default, users can use separate video button.
    // For true photo+video switching, use PHOTO mode + VIDEO mode separately.
    //
    // Here we default to photo capture since Android's native camera app
    // typically lets users switch to video mode themselves.
    val photoUri = remember { createTempUri(context, "camera_media", ".jpg") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            scope.launch {
                val result = readMediaResult(context, photoUri, "camera_media.jpg", "image/jpeg")
                onResult(result)
            }
        } else {
            onResult(null)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(photoUri)
        } else {
            onResult(null)
        }
    }

    return remember(cameraLauncher, permissionLauncher) {
        object : CameraLauncher {
            override fun launch() {
                if (hasCameraPermission(context)) {
                    cameraLauncher.launch(photoUri)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

private fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

private fun createTempUri(context: Context, prefix: String, extension: String): Uri {
    val tempFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}$extension")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

private suspend fun readMediaResult(
    context: Context,
    uri: Uri,
    fallbackName: String,
    fallbackMimeType: String
): MediaResult? = withContext(Dispatchers.IO) {
    try {
        val contentResolver = context.contentResolver
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@withContext null
        val mimeType = contentResolver.getType(uri) ?: fallbackMimeType

        MediaResult(
            bytes = bytes,
            fileName = fallbackName,
            mimeType = mimeType,
            size = bytes.size.toLong()
        )
    } catch (_: Exception) {
        null
    }
}

private suspend fun readVideoResult(
    context: Context,
    uri: Uri
): MediaResult? = withContext(Dispatchers.IO) {
    try {
        val contentResolver = context.contentResolver
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@withContext null
        val mimeType = contentResolver.getType(uri) ?: "video/mp4"

        // Generate thumbnail from first frame
        val thumbnailBytes = try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            if (bitmap != null) {
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, stream)
                stream.toByteArray()
            } else null
        } catch (_: Exception) {
            null
        }

        MediaResult(
            bytes = bytes,
            fileName = "camera_video_${System.currentTimeMillis()}.mp4",
            mimeType = mimeType,
            size = bytes.size.toLong(),
            thumbnailBytes = thumbnailBytes
        )
    } catch (_: Exception) {
        null
    }
}
