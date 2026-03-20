package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.marketfinder.core.data.media.LatLng
import com.marketfinder.core.data.media.MapMarker

@Composable
actual fun PlatformMapView(
    center: LatLng,
    markers: List<MapMarker>,
    zoom: Float,
    onMarkerClick: ((MapMarker) -> Unit)?,
    onMapClick: ((LatLng) -> Unit)?,
    modifier: Modifier
) {
    val googleCenter = com.google.android.gms.maps.model.LatLng(center.latitude, center.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(googleCenter, zoom)
    }

    // Update camera when center changes
    LaunchedEffect(center, zoom) {
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(googleCenter, zoom)
            )
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            onMapClick?.invoke(LatLng(latLng.latitude, latLng.longitude))
        }
    ) {
        markers.forEach { marker ->
            val position = com.google.android.gms.maps.model.LatLng(
                marker.position.latitude,
                marker.position.longitude
            )
            Marker(
                state = MarkerState(position = position),
                title = marker.title,
                snippet = marker.snippet,
                onClick = {
                    onMarkerClick?.invoke(marker)
                    true
                }
            )
        }
    }
}
