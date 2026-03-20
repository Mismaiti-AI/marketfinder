package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import com.marketfinder.core.data.media.MediaPickerType
import com.marketfinder.core.data.media.MediaResult

/**
 * Handle returned by [rememberMediaPickerLauncher] to trigger the system picker.
 */
interface MediaPickerLauncher {
    fun launch()
}

/**
 * Creates and remembers a media/file picker launcher.
 *
 * Opens the device's native picker based on the [type]:
 * - [MediaPickerType.IMAGE] / [MediaPickerType.VIDEO] / [MediaPickerType.IMAGE_AND_VIDEO]:
 *   Opens the photo/video picker.
 * - [MediaPickerType.FILE]: Opens the document picker for any file type.
 *
 * The selected file is returned as a [MediaResult] via the [onResult] callback.
 * Returns `null` if the user cancels.
 *
 * - **Android**: Uses `PickVisualMedia` or `OpenDocument` activity result contracts.
 * - **iOS**: Uses `PHPickerViewController` or `UIDocumentPickerViewController`.
 */
@Composable
expect fun rememberMediaPickerLauncher(
    type: MediaPickerType,
    onResult: (MediaResult?) -> Unit
): MediaPickerLauncher
