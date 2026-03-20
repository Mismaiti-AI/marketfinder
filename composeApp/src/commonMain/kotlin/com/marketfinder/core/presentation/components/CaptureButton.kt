package com.marketfinder.core.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.media.MediaPickerType
import com.marketfinder.core.data.media.MediaResult
import com.marketfinder.core.presentation.media.CameraCaptureMode
import com.marketfinder.core.presentation.media.rememberCameraLauncher
import com.marketfinder.core.presentation.media.rememberMediaPickerLauncher

/**
 * Pre-styled button that opens the device camera and returns captured media as [MediaResult].
 *
 * Usage:
 * ```
 * // Photo only (default)
 * CaptureButton(onCaptured = { photo = it })
 *
 * // Video only
 * CaptureButton(mode = CameraCaptureMode.VIDEO, label = "Record Video", onCaptured = { video = it })
 *
 * // Photo + Video (user can switch in camera UI)
 * CaptureButton(mode = CameraCaptureMode.PHOTO_AND_VIDEO, label = "Capture", onCaptured = { media = it })
 * ```
 */
@Composable
fun CaptureButton(
    onCaptured: (MediaResult?) -> Unit,
    modifier: Modifier = Modifier,
    mode: CameraCaptureMode = CameraCaptureMode.PHOTO,
    label: String = "Take Photo",
    icon: ImageVector = Icons.Default.CameraAlt
) {
    val cameraLauncher = rememberCameraLauncher(mode = mode, onResult = onCaptured)

    Button(
        onClick = { cameraLauncher.launch() },
        modifier = modifier
    ) {
        Icon(imageVector = icon, contentDescription = label)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

/**
 * Pre-styled button that opens the system media/file picker and returns selected item as [MediaResult].
 *
 * Usage:
 * ```
 * var image by remember { mutableStateOf<MediaResult?>(null) }
 * PickMediaButton(type = MediaPickerType.IMAGE, onPicked = { image = it })
 * ```
 */
@Composable
fun PickMediaButton(
    onPicked: (MediaResult?) -> Unit,
    modifier: Modifier = Modifier,
    type: MediaPickerType = MediaPickerType.IMAGE,
    label: String = "Pick File",
    icon: ImageVector = Icons.Default.AttachFile
) {
    val pickerLauncher = rememberMediaPickerLauncher(type = type, onResult = onPicked)

    Button(
        onClick = { pickerLauncher.launch() },
        modifier = modifier
    ) {
        Icon(imageVector = icon, contentDescription = label)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}
