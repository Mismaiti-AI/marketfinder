package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.isActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentItem
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformVideoPlayer(
    url: String,
    isPlaying: Boolean,
    onPlayingChange: (Boolean) -> Unit,
    modifier: Modifier,
    onReady: ((durationMs: Long) -> Unit)?,
    onProgress: ((positionMs: Long) -> Unit)?
) {
    val player = remember { AVPlayer() }
    val playerViewController = remember {
        AVPlayerViewController().apply { this.player = player }
    }

    // Update media source when URL changes
    LaunchedEffect(url) {
        val nsUrl = NSURL.URLWithString(url) ?: return@LaunchedEffect
        val playerItem = AVPlayerItem(uRL = nsUrl)
        player.replaceCurrentItemWithPlayerItem(playerItem)
    }

    // Sync play state
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
    }

    // Observe ready state for duration
    if (onReady != null) {
        LaunchedEffect(url) {
            kotlinx.coroutines.delay(100)
            while (isActive) {
                val item = player.currentItem
                if (item != null && item.status == AVPlayerItemStatusReadyToPlay) {
                    val durationSeconds = CMTimeGetSeconds(item.duration)
                    if (!durationSeconds.isNaN() && !durationSeconds.isInfinite()) {
                        onReady((durationSeconds * 1000).toLong())
                        break
                    }
                }
                kotlinx.coroutines.delay(200)
            }
        }
    }

    // Progress observer
    DisposableEffect(player) {
        var observer: Any? = null
        if (onProgress != null) {
            val interval = CMTimeMakeWithSeconds(0.5, 600)
            observer = player.addPeriodicTimeObserverForInterval(
                interval = interval,
                queue = null
            ) { time: CValue<CMTime> ->
                val seconds = CMTimeGetSeconds(time)
                if (!seconds.isNaN()) {
                    onProgress((seconds * 1000).toLong())
                }
            }
        }

        onDispose {
            observer?.let { player.removeTimeObserver(it) }
            player.pause()
        }
    }

    UIKitView(
        factory = { playerViewController.view },
        modifier = modifier
    )
}
