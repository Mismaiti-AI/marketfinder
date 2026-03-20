package com.marketfinder.core.data.media

/**
 * Result from camera capture or media/file picker.
 *
 * Contains the raw bytes and metadata of the captured/selected media.
 * Used by [CameraLauncher] and [MediaPickerLauncher] callbacks.
 */
data class MediaResult(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    /** Optional thumbnail image bytes (JPEG) for video previews. */
    val thumbnailBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaResult) return false
        return bytes.contentEquals(other.bytes) &&
                fileName == other.fileName &&
                mimeType == other.mimeType &&
                size == other.size &&
                (thumbnailBytes?.contentEquals(other.thumbnailBytes ?: byteArrayOf()) ?: (other.thumbnailBytes == null))
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + (thumbnailBytes?.contentHashCode() ?: 0)
        return result
    }
}
