package com.marketfinder.core.data.media

/**
 * Types of media that can be selected via the system picker.
 *
 * - [IMAGE] — Photos only
 * - [VIDEO] — Videos only
 * - [IMAGE_AND_VIDEO] — Both photos and videos
 * - [FILE] — Any file type via document picker
 */
enum class MediaPickerType {
    IMAGE,
    VIDEO,
    IMAGE_AND_VIDEO,
    FILE
}
