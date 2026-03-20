package com.marketfinder.core.presentation.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.marketfinder.core.data.media.LatLng
import com.marketfinder.core.data.media.MapMarker
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKPointAnnotation
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun PlatformMapView(
    center: LatLng,
    markers: List<MapMarker>,
    zoom: Float,
    onMarkerClick: ((MapMarker) -> Unit)?,
    onMapClick: ((LatLng) -> Unit)?,
    modifier: Modifier
) {
    val markerMap = remember { mutableMapOf<MKPointAnnotation, MapMarker>() }

    val delegate = remember(onMarkerClick) {
        MapViewDelegate(markerMap, onMarkerClick)
    }

    val mapView = remember {
        MKMapView().apply {
            this.delegate = delegate
        }
    }

    // Keep delegate in sync
    LaunchedEffect(delegate) {
        mapView.delegate = delegate
    }

    // Update region when center/zoom changes
    LaunchedEffect(center, zoom) {
        val coordinate = CLLocationCoordinate2DMake(center.latitude, center.longitude)
        val distance = 40_000_000.0 / pow2(zoom.toDouble())
        val region = MKCoordinateRegionMakeWithDistance(coordinate, distance, distance)
        mapView.setRegion(region, animated = true)
    }

    // Update markers
    LaunchedEffect(markers) {
        mapView.removeAnnotations(mapView.annotations)
        markerMap.clear()

        markers.forEach { marker ->
            val annotation = MKPointAnnotation().apply {
                setCoordinate(
                    CLLocationCoordinate2DMake(
                        marker.position.latitude,
                        marker.position.longitude
                    )
                )
                setTitle(marker.title)
                marker.snippet?.let { setSubtitle(it) }
            }
            markerMap[annotation] = marker
            mapView.addAnnotation(annotation)
        }
    }

    UIKitView(
        factory = { mapView },
        modifier = modifier
    )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class MapViewDelegate(
    private val markerMap: Map<MKPointAnnotation, MapMarker>,
    private val onMarkerClick: ((MapMarker) -> Unit)?
) : NSObject(), MKMapViewDelegateProtocol {

    override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
        val annotation = didSelectAnnotationView.annotation as? MKPointAnnotation ?: return
        val marker = markerMap[annotation] ?: return
        onMarkerClick?.invoke(marker)
        mapView.deselectAnnotation(annotation, animated = true)
    }

    override fun mapView(
        mapView: MKMapView,
        regionDidChangeAnimated: Boolean
    ) {
        // Map region changed — could be used for additional callbacks
    }
}

private fun pow2(exp: Double): Double {
    // Handle fractional exponents: split into integer + fractional parts
    val intPart = exp.toInt()
    val fracPart = exp - intPart
    var result = 1.0
    repeat(intPart) { result *= 2.0 }
    // Linear interpolation for fractional part: 2^frac ≈ 1 + frac * ln(2)
    // More accurate: use successive squaring approximation
    if (fracPart > 0.0) {
        // 2^frac using exp(frac * ln2), approximated via Taylor series
        // For map zoom (0..22), this is accurate enough
        val ln2 = 0.693147180559945
        val x = fracPart * ln2
        result *= (1.0 + x + x * x / 2.0 + x * x * x / 6.0)
    }
    return result
}
