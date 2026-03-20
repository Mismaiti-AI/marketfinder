package com.marketfinder.core.presentation.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.marketfinder.core.data.media.MediaPickerType
import com.marketfinder.core.data.media.MediaResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberMediaPickerLauncher(
    type: MediaPickerType,
    onResult: (MediaResult?) -> Unit
): MediaPickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return if (type == MediaPickerType.FILE) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    val result = readUriToMediaResult(context, uri)
                    onResult(result)
                }
            } else {
                onResult(null)
            }
        }

        remember(launcher) {
            object : MediaPickerLauncher {
                override fun launch() {
                    launcher.launch(arrayOf("*/*"))
                }
            }
        }
    } else {
        val mediaType = when (type) {
            MediaPickerType.IMAGE -> ActivityResultContracts.PickVisualMedia.ImageOnly
            MediaPickerType.VIDEO -> ActivityResultContracts.PickVisualMedia.VideoOnly
            else -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    val result = readUriToMediaResult(context, uri)
                    onResult(result)
                }
            } else {
                onResult(null)
            }
        }

        remember(launcher) {
            object : MediaPickerLauncher {
                override fun launch() {
                    launcher.launch(PickVisualMediaRequest(mediaType))
                }
            }
        }
    }
}

private suspend fun readUriToMediaResult(context: Context, uri: Uri): MediaResult? =
    withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext null
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

            var fileName = "picked_file"
            var fileSize = bytes.size.toLong()

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            // Generate thumbnail for videos and PDFs
            val thumbnailBytes = when {
                mimeType.startsWith("video/") -> generateVideoThumbnail(context, uri)
                mimeType == "application/pdf" -> generatePdfThumbnail(context, uri)
                else -> null
            }

            MediaResult(
                bytes = bytes,
                fileName = fileName,
                mimeType = mimeType,
                size = fileSize,
                thumbnailBytes = thumbnailBytes
            )
        } catch (e: Exception) {
            null
        }
    }

private fun generateVideoThumbnail(context: Context, uri: Uri): ByteArray? {
    return try {
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
}

private fun generatePdfThumbnail(context: Context, uri: Uri): ByteArray? {
    return try {
        val fd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
        val renderer = android.graphics.pdf.PdfRenderer(fd)
        val page = renderer.openPage(0)
        val bitmap = android.graphics.Bitmap.createBitmap(
            page.width * 2, page.height * 2, android.graphics.Bitmap.Config.ARGB_8888
        )
        // White background
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        fd.close()
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
        stream.toByteArray()
    } catch (_: Exception) {
        null
    }
}
