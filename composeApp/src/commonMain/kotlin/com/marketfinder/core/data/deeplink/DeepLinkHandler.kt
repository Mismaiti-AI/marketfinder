package com.marketfinder.core.data.deeplink

import kotlinx.coroutines.flow.StateFlow

/**
 * Deep Link Handler - Pre-Built Template Component
 *
 * Manages incoming deep link URIs with deferred consumption.
 * Deep links arriving during Splash/Auth are stored as pending
 * and consumed once the Home state activates MainScaffold.
 *
 * Usage:
 * ```
 * // In platform code (Activity/AppDelegate):
 * val handler: DeepLinkHandler = koinInject()
 * handler.handleIncomingUri("mismaiti://detail/123")
 *
 * // In MainScaffold's LaunchedEffect:
 * val uri = deepLinkHandler.pendingDeepLink.collectAsState().value
 * if (uri != null) {
 *     // route based on URI
 *     deepLinkHandler.consumeDeepLink()
 * }
 * ```
 */
interface DeepLinkHandler {
    val pendingDeepLink: StateFlow<String?>
    fun handleIncomingUri(uri: String)
    fun consumeDeepLink()
}
