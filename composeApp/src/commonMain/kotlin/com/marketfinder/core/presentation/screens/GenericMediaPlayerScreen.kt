package com.marketfinder.core.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.marketfinder.core.presentation.components.AudioPlayerBar
import com.marketfinder.core.presentation.components.SectionHeader
import com.marketfinder.core.presentation.components.VideoPlayerView

/**
 * Media player item data — represents a video or audio track.
 */
data class MediaPlayerItem(
    val title: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val subtitle: String? = null,
    val duration: String? = null,
    val isAudio: Boolean = false
)

/**
 * Generic Media Player Screen - Pre-Built Template Screen
 *
 * Displays a video or audio player at the top, detail content below, and optional related items list.
 *
 * Usage:
 * ```
 * GenericMediaPlayerScreen(
 *     title = "Lesson 1",
 *     item = MediaPlayerItem(
 *         title = lesson.title,
 *         url = lesson.videoUrl,
 *         thumbnailUrl = lesson.thumbnail
 *     ),
 *     onBackClick = { navController.popBackStack() },
 *     relatedItems = otherLessons.map { MediaPlayerItem(it.title, it.url) },
 *     onRelatedItemClick = { navController.navigate(LessonDetail(it.url)) }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericMediaPlayerScreen(
    title: String,
    item: MediaPlayerItem?,
    isLoading: Boolean = false,
    onBackClick: () -> Unit = {},
    showBack: Boolean = true,
    relatedItems: List<MediaPlayerItem> = emptyList(),
    onRelatedItemClick: (MediaPlayerItem) -> Unit = {},
    detailContent: (@Composable (MediaPlayerItem) -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                item == null -> {
                    Text(
                        text = "No media available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // Player
                        item {
                            if (item.isAudio) {
                                AudioPlayerBar(
                                    url = item.url,
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    thumbnailUrl = item.thumbnailUrl,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                VideoPlayerView(
                                    url = item.url,
                                    autoPlay = true,
                                    showControls = true
                                )
                            }
                        }

                        // Title and subtitle
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.subtitle != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Custom detail content
                        if (detailContent != null) {
                            item {
                                detailContent(item)
                            }
                        }

                        // Related items
                        if (relatedItems.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Related",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            items(relatedItems) { relatedItem ->
                                RelatedMediaItem(
                                    item = relatedItem,
                                    onClick = { onRelatedItemClick(relatedItem) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedMediaItem(
    item: MediaPlayerItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        if (item.thumbnailUrl != null) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(width = 120.dp, height = 68.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 68.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (item.duration != null) {
                Text(
                    text = item.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
