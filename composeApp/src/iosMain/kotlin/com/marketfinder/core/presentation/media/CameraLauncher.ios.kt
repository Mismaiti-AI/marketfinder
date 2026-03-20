package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.marketfinder.core.data.media.MediaResult
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraCaptureMode
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun rememberCameraLauncher(
    mode: CameraCaptureMode,
    onResult: (MediaResult?) -> Unit
): CameraLauncher {
    return remember(mode) {
        object : CameraLauncher {
            override fun launch() {
                MainScope().launch {
                    val result = launchCamera(mode)
                    onResult(result)
                }
            }
        }
    }
}

// Strong reference to prevent GC while picker is open (iOS delegate is weak)
private var activeCameraDelegate: CameraDelegate? = null

private suspend fun launchCamera(mode: CameraCaptureMode): MediaResult? {
    // Check if camera is available (not available on Simulator)
    if (!UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        )
    ) {
        return null
    }

    val deferred = CompletableDeferred<MediaResult?>()

    val delegate = CameraDelegate(deferred)
    activeCameraDelegate = delegate
    val picker = UIImagePickerController().apply {
        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        this.delegate = delegate

        when (mode) {
            CameraCaptureMode.PHOTO -> {
                mediaTypes = listOf("public.image")
                cameraCaptureMode = UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModePhoto
            }
            CameraCaptureMode.VIDEO -> {
                mediaTypes = listOf("public.movie")
                cameraCaptureMode = UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModeVideo
            }
            CameraCaptureMode.PHOTO_AND_VIDEO -> {
                mediaTypes = listOf("public.image", "public.movie")
                cameraCaptureMode = UIImagePickerControllerCameraCaptureMode.UIImagePickerControllerCameraCaptureModePhoto
            }
        }
    }

    val rootViewController = getRootViewController()
    rootViewController?.presentViewController(picker, animated = true, completion = null)
        ?: deferred.complete(null)

    return try {
        deferred.await()
    } finally {
        activeCameraDelegate = null
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class CameraDelegate(
    private val deferred: CompletableDeferred<MediaResult?>
) : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)

        // Try image first
        val image = (didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage]
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage]) as? UIImage

        if (image != null) {
            val jpegData = UIImageJPEGRepresentation(image, 0.85)
            if (jpegData == null) {
                deferred.complete(null)
                return
            }

            val bytes = nsDataToByteArray(jpegData)
            deferred.complete(
                MediaResult(
                    bytes = bytes,
                    fileName = "camera_photo_${NSDate().timeIntervalSince1970.toLong()}.jpg",
                    mimeType = "image/jpeg",
                    size = bytes.size.toLong()
                )
            )
            return
        }

        // Try video URL
        val videoUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaURL] as? NSURL
        if (videoUrl != null) {
            val data = NSData.dataWithContentsOfURL(videoUrl)
            if (data == null) {
                deferred.complete(null)
                return
            }

            val bytes = nsDataToByteArray(data)
            val fileName = videoUrl.lastPathComponent
                ?: "camera_video_${NSDate().timeIntervalSince1970.toLong()}.mp4"

            // Extract thumbnail from first frame
            val thumbnailBytes = generateVideoThumbnail(videoUrl)

            deferred.complete(
                MediaResult(
                    bytes = bytes,
                    fileName = fileName,
                    mimeType = "video/mp4",
                    size = bytes.size.toLong(),
                    thumbnailBytes = thumbnailBytes
                )
            )
            return
        }

        deferred.complete(null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        deferred.complete(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun generateVideoThumbnail(videoUrl: NSURL): ByteArray? {
    return try {
        val asset = AVURLAsset(uRL = videoUrl, options = null)
        val generator = AVAssetImageGenerator(asset = asset)
        generator.appliesPreferredTrackTransform = true
        val time = CMTimeMake(value = 0, timescale = 1)
        val cgImage = generator.copyCGImageAtTime(time, actualTime = null, error = null)
            ?: return null
        val uiImage = UIImage(cGImage = cgImage)
        val jpegData = UIImageJPEGRepresentation(uiImage, 0.7) ?: return null
        nsDataToByteArray(jpegData)
    } catch (_: Exception) {
        null
    }
}
