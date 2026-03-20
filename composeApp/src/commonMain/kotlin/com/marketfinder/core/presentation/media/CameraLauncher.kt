package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import com.marketfinder.core.data.media.MediaResult

/**
 * Handle returned by [rememberCameraLauncher] to trigger camera capture.
 */
interface CameraLauncher {
    fun launch()
}

/** What the camera should capture. */
enum class CameraCaptureMode {
    /** Photo only (default). */
    PHOTO,
    /** Video only. */
    VIDEO,
    /** User can switch between photo and video in the native camera UI. */
    PHOTO_AND_VIDEO
}

/**
 * Creates and remembers a camera launcher that opens the device's native camera app.
 *
 * @param mode What to capture — photo, video, or both (default: [CameraCaptureMode.PHOTO]).
 * @param onResult Called with the captured [MediaResult], or `null` if user cancels/denies permission.
 *   For video captures, [MediaResult.thumbnailBytes] contains a JPEG thumbnail of the first frame.
 *
 * **Permissions are handled automatically:**
 * - **Android**: Requests `CAMERA` permission at runtime before launching. If denied, [onResult] receives `null`.
 * - **iOS**: System auto-prompts via `NSCameraUsageDescription` in Info.plist when camera is first accessed.
 */
@Composable
expect fun rememberCameraLauncher(
    mode: CameraCaptureMode = CameraCaptureMode.PHOTO,
    onResult: (MediaResult?) -> Unit
): CameraLauncher
