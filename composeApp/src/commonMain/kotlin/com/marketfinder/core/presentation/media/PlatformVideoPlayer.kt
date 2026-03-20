package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-native video/audio player composable.
 *
 * - **Android**: Uses Media3 ExoPlayer wrapped in `AndroidView`.
 * - **iOS**: Uses AVPlayerViewController wrapped in `UIKitView`.
 *
 * @param url Media URL to play (remote or local file path).
 * @param isPlaying Whether the player should be playing.
 * @param onPlayingChange Called when play state changes (e.g. user pauses via native controls).
 * @param modifier Compose modifier for sizing/layout.
 * @param onReady Called when the player is ready with the total duration in milliseconds.
 * @param onProgress Called periodically with the current playback position in milliseconds.
 */
@Composable
expect fun PlatformVideoPlayer(
    url: String,
    isPlaying: Boolean,
    onPlayingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onReady: ((durationMs: Long) -> Unit)? = null,
    onProgress: ((positionMs: Long) -> Unit)? = null
)
