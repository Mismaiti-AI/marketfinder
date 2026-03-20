package com.marketfinder.core.data.media

/**
 * Geographic coordinate pair.
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

/**
 * Map marker with position, title, and optional snippet.
 */
data class MapMarker(
    val position: LatLng,
    val title: String,
    val snippet: String? = null
)
