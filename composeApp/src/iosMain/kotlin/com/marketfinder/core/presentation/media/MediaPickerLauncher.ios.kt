package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.marketfinder.core.data.media.MediaPickerType
import com.marketfinder.core.data.media.MediaResult
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.timeIntervalSince1970
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVURLAsset
import platform.CoreGraphics.CGSizeMake
import platform.CoreMedia.CMTimeMake
import platform.PDFKit.PDFDocument
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeContent
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberMediaPickerLauncher(
    type: MediaPickerType,
    onResult: (MediaResult?) -> Unit
): MediaPickerLauncher {
    return remember(type) {
        object : MediaPickerLauncher {
            override fun launch() {
                MainScope().launch {
                    val result = when (type) {
                        MediaPickerType.FILE -> launchDocumentPicker()
                        else -> launchImagePickerController(type)
                    }
                    onResult(result)
                }
            }
        }
    }
}

// Strong references to prevent GC while picker is open (iOS delegates are weak)
private var activePickerDelegate: ImagePickerDelegate? = null
private var activeDocumentDelegate: DocumentPickerDelegate? = null

private suspend fun launchImagePickerController(type: MediaPickerType): MediaResult? {
    val deferred = CompletableDeferred<MediaResult?>()

    val delegate = ImagePickerDelegate(deferred)
    activePickerDelegate = delegate

    val picker = UIImagePickerController().apply {
        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        this.delegate = delegate
        when (type) {
            MediaPickerType.IMAGE -> mediaTypes = listOf("public.image")
            MediaPickerType.VIDEO -> mediaTypes = listOf("public.movie")
            MediaPickerType.IMAGE_AND_VIDEO -> mediaTypes = listOf("public.image", "public.movie")
            else -> {}
        }
    }

    val rootViewController = getRootViewController()
    rootViewController?.presentViewController(picker, animated = true, completion = null)
        ?: deferred.complete(null)

    return try {
        deferred.await()
    } finally {
        activePickerDelegate = null
    }
}

private suspend fun launchDocumentPicker(): MediaResult? {
    val deferred = CompletableDeferred<MediaResult?>()

    val contentType = UTTypeContent ?: return null
    val delegate = DocumentPickerDelegate(deferred)
    activeDocumentDelegate = delegate
    val picker = UIDocumentPickerViewController(
        forOpeningContentTypes = listOf(contentType)
    ).apply {
        this.delegate = delegate
        this.allowsMultipleSelection = false
    }

    val rootViewController = getRootViewController()
    rootViewController?.presentViewController(picker, animated = true, completion = null)
        ?: deferred.complete(null)

    return try {
        deferred.await()
    } finally {
        activeDocumentDelegate = null
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun nsDataToByteArray(data: NSData): ByteArray {
    val size = data.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        memcpy(bytes.refTo(0), data.bytes, data.length)
    }
    return bytes
}

internal fun getRootViewController() =
    (UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene)
        ?.windows
        ?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true }
        ?.let { (it as? UIWindow)?.rootViewController }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class ImagePickerDelegate(
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
            val jpegData = UIImageJPEGRepresentation(image, 0.9)
            if (jpegData == null) {
                deferred.complete(null)
                return
            }

            val bytes = nsDataToByteArray(jpegData)
            deferred.complete(
                MediaResult(
                    bytes = bytes,
                    fileName = "picked_image_${NSDate().timeIntervalSince1970.toLong()}.jpg",
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
                ?: "picked_video_${NSDate().timeIntervalSince1970.toLong()}.mp4"

            // Extract thumbnail from the first frame
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

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class DocumentPickerDelegate(
    private val deferred: CompletableDeferred<MediaResult?>
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            deferred.complete(null)
            return
        }

        val accessing = url.startAccessingSecurityScopedResource()
        try {
            val data = NSData.dataWithContentsOfURL(url)
            if (data == null) {
                deferred.complete(null)
                return
            }

            val bytes = nsDataToByteArray(data)
            val fileName = url.lastPathComponent ?: "picked_file"
            val ext = fileName.substringAfterLast('.', "").lowercase()
            val mimeType = when (ext) {
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "mp4", "m4v" -> "video/mp4"
                "mov" -> "video/quicktime"
                "mp3" -> "audio/mpeg"
                "m4a" -> "audio/mp4"
                else -> "application/octet-stream"
            }

            val thumbnailBytes = if (mimeType == "application/pdf") {
                generatePdfThumbnail(url)
            } else null

            deferred.complete(
                MediaResult(
                    bytes = bytes,
                    fileName = fileName,
                    mimeType = mimeType,
                    size = bytes.size.toLong(),
                    thumbnailBytes = thumbnailBytes
                )
            )
        } finally {
            if (accessing) {
                url.stopAccessingSecurityScopedResource()
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
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

@OptIn(ExperimentalForeignApi::class)
private fun generatePdfThumbnail(pdfUrl: NSURL): ByteArray? {
    return try {
        val document = PDFDocument(uRL = pdfUrl) ?: return null
        val page = document.pageAtIndex(0u) ?: return null
        // Use a fixed thumbnail size — PDFKit scales proportionally
        val thumbnailSize = CGSizeMake(600.0, 800.0)
        val thumbnail = page.thumbnailOfSize(thumbnailSize, forBox = platform.PDFKit.kPDFDisplayBoxMediaBox)
        val jpegData = UIImageJPEGRepresentation(thumbnail, 0.8) ?: return null
        nsDataToByteArray(jpegData)
    } catch (_: Exception) {
        null
    }
}
