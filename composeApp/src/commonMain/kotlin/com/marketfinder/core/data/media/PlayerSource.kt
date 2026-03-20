package com.marketfinder.core.data.media

/**
 * Source for audio/video playback.
 *
 * - [Url] — Stream from a remote URL
 * - [LocalFile] — Play from a local file path
 */
sealed class PlayerSource {
    data class Url(val url: String) : PlayerSource()
    data class LocalFile(val path: String) : PlayerSource()
}
