package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.marketfinder.core.data.media.LatLng
import com.marketfinder.core.data.media.MapMarker

/**
 * Platform-native map composable.
 *
 * - **Android**: Uses Google Maps via `maps-compose` library.
 * - **iOS**: Uses Apple MapKit via `MKMapView` in `UIKitView`.
 *
 * @param center The center coordinate of the map.
 * @param markers List of markers to display on the map.
 * @param zoom Zoom level (higher = more zoomed in).
 * @param onMarkerClick Called when a marker is tapped.
 * @param onMapClick Called when the map background is tapped with the coordinates.
 * @param modifier Compose modifier for sizing/layout.
 */
@Composable
expect fun PlatformMapView(
    center: LatLng,
    markers: List<MapMarker> = emptyList(),
    zoom: Float = 12f,
    onMarkerClick: ((MapMarker) -> Unit)? = null,
    onMapClick: ((LatLng) -> Unit)? = null,
    modifier: Modifier = Modifier
)
