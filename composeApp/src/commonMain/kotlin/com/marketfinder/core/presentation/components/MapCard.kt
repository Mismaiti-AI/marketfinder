package com.marketfinder.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.media.LatLng
import com.marketfinder.core.data.media.MapMarker
import com.marketfinder.core.presentation.media.PlatformMapView

/**
 * Card-shaped map preview for detail screens and list items.
 *
 * Wraps [PlatformMapView] in a Material card with configurable height.
 *
 * Usage:
 * ```
 * MapCard(
 *     center = LatLng(store.latitude, store.longitude),
 *     markers = listOf(MapMarker(LatLng(store.lat, store.lng), store.name)),
 *     onClick = { navController.navigate(MapRoute) }
 * )
 * ```
 */
@Composable
fun MapCard(
    center: LatLng,
    modifier: Modifier = Modifier,
    markers: List<MapMarker> = emptyList(),
    zoom: Float = 14f,
    height: Dp = 200.dp,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        PlatformMapView(
            center = center,
            markers = markers,
            zoom = zoom,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        )
    }
}
