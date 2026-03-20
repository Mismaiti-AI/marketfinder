package com.marketfinder.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.media.MediaResult
import com.marketfinder.core.presentation.media.decodeByteArrayToImageBitmap

/**
 * Visual preview of a captured or picked media file.
 *
 * - Images: renders the actual image from [MediaResult.bytes]
 * - Videos: shows a video icon with file info
 * - Audio: shows an audio icon with file info
 * - Other files: shows a generic file icon with file info
 *
 * Usage:
 * ```
 * var photo by remember { mutableStateOf<MediaResult?>(null) }
 * CaptureButton(onCaptured = { photo = it })
 * photo?.let {
 *     MediaPreview(result = it, onRemove = { photo = null })
 * }
 * ```
 */
@Composable
fun MediaPreview(
    result: MediaResult,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    onRemove: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            when {
                result.mimeType.startsWith("image/") -> {
                    ImagePreview(result = result, height = height, onRemove = onRemove)
                }
                result.mimeType.startsWith("video/") -> {
                    if (result.thumbnailBytes != null) {
                        ThumbnailPreview(
                            thumbnailBytes = result.thumbnailBytes,
                            contentDescription = result.fileName,
                            overlayIcon = Icons.Default.Videocam,
                            height = height,
                            onRemove = onRemove
                        )
                    } else {
                        FileTypePreview(
                            result = result,
                            icon = Icons.Default.Videocam,
                            typeLabel = "Video",
                            height = height,
                            onRemove = onRemove
                        )
                    }
                }
                result.mimeType.startsWith("audio/") -> {
                    FileTypePreview(
                        result = result,
                        icon = Icons.Default.AudioFile,
                        typeLabel = "Audio",
                        height = height,
                        onRemove = onRemove
                    )
                }
                else -> {
                    val fileIcon = if (result.mimeType == "application/pdf") {
                        Icons.Default.PictureAsPdf
                    } else {
                        Icons.Default.InsertDriveFile
                    }
                    if (result.thumbnailBytes != null) {
                        ThumbnailPreview(
                            thumbnailBytes = result.thumbnailBytes,
                            contentDescription = result.fileName,
                            overlayIcon = fileIcon,
                            height = height,
                            onRemove = onRemove
                        )
                    } else {
                        val typeLabel = if (result.mimeType == "application/pdf") "PDF" else "File"
                        FileTypePreview(
                            result = result,
                            icon = fileIcon,
                            typeLabel = typeLabel,
                            height = height,
                            onRemove = onRemove
                        )
                    }
                }
            }

            // File info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatFileSize(result.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ImagePreview(
    result: MediaResult,
    height: Dp,
    onRemove: (() -> Unit)?
) {
    val imageBitmap: ImageBitmap? = remember(result.bytes) {
        try {
            decodeByteArrayToImageBitmap(result.bytes)
        } catch (_: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = result.fileName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback if decoding fails
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unable to load image", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (onRemove != null) {
            RemoveButton(onRemove)
        }
    }
}

@Composable
private fun ThumbnailPreview(
    thumbnailBytes: ByteArray,
    contentDescription: String,
    overlayIcon: androidx.compose.ui.graphics.vector.ImageVector,
    height: Dp,
    onRemove: (() -> Unit)?
) {
    val imageBitmap: ImageBitmap? = remember(thumbnailBytes) {
        try {
            decodeByteArrayToImageBitmap(thumbnailBytes)
        } catch (_: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Play icon overlay
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Icon(
                    imageVector = overlayIcon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        if (onRemove != null) {
            RemoveButton(onRemove)
        }
    }
}

@Composable
private fun FileTypePreview(
    result: MediaResult,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    typeLabel: String,
    height: Dp,
    onRemove: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (onRemove != null) {
            RemoveButton(onRemove)
        }
    }
}

@Composable
private fun RemoveButton(onRemove: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.padding(4.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> {
            val mb = bytes / (1024.0 * 1024.0)
            val mbStr = if (mb < 10) {
                val whole = (mb * 10).toLong()
                "${whole / 10}.${whole % 10}"
            } else {
                "${mb.toLong()}"
            }
            "$mbStr MB"
        }
    }
}
