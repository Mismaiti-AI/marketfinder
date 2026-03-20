package com.marketfinder.core.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.media.LatLng
import com.marketfinder.core.data.media.MapMarker
import com.marketfinder.core.presentation.media.PlatformMapView

/**
 * Map screen configuration — initial center and zoom.
 */
data class MapScreenConfig(
    val initialCenter: LatLng,
    val initialZoom: Float = 12f
)

/**
 * Generic Map Screen - Pre-Built Template Screen
 *
 * Full-screen map with TopAppBar overlay, optional search bar, and optional bottom sheet.
 *
 * Usage:
 * ```
 * GenericMapScreen(
 *     title = "Store Locations",
 *     config = MapScreenConfig(initialCenter = LatLng(37.7749, -122.4194)),
 *     markers = stores.map { MapMarker(LatLng(it.lat, it.lng), it.name) },
 *     onMarkerClick = { viewModel.selectStore(it) },
 *     bottomSheetContent = {
 *         // Selected store details
 *         StoreDetailCard(selectedStore)
 *     }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericMapScreen(
    title: String,
    config: MapScreenConfig,
    markers: List<MapMarker> = emptyList(),
    onBackClick: () -> Unit = {},
    showBack: Boolean = true,
    onMarkerClick: ((MapMarker) -> Unit)? = null,
    onMapClick: ((LatLng) -> Unit)? = null,
    showSearchBar: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    bottomSheetContent: (@Composable () -> Unit)? = null
) {
    if (bottomSheetContent != null) {
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded
            )
        )

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 128.dp,
            sheetContent = {
                bottomSheetContent()
            },
            topBar = {
                MapTopBar(title, showBack, onBackClick)
            }
        ) { padding ->
            MapContent(
                config = config,
                markers = markers,
                onMarkerClick = onMarkerClick,
                onMapClick = onMapClick,
                showSearchBar = showSearchBar,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearchSubmit = onSearchSubmit,
                modifier = Modifier.padding(padding)
            )
        }
    } else {
        Scaffold(
            topBar = {
                MapTopBar(title, showBack, onBackClick)
            }
        ) { padding ->
            MapContent(
                config = config,
                markers = markers,
                onMarkerClick = onMarkerClick,
                onMapClick = onMapClick,
                showSearchBar = showSearchBar,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearchSubmit = onSearchSubmit,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapTopBar(
    title: String,
    showBack: Boolean,
    onBackClick: () -> Unit
) {
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

@Composable
private fun MapContent(
    config: MapScreenConfig,
    markers: List<MapMarker>,
    onMarkerClick: ((MapMarker) -> Unit)?,
    onMapClick: ((LatLng) -> Unit)?,
    showSearchBar: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        PlatformMapView(
            center = config.initialCenter,
            markers = markers,
            zoom = config.initialZoom,
            onMarkerClick = onMarkerClick,
            onMapClick = onMapClick,
            modifier = Modifier.fillMaxSize()
        )

        if (showSearchBar) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search location...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit(searchQuery) })
            )
        }
    }
}
