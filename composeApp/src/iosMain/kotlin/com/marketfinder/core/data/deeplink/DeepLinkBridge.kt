package com.marketfinder.core.data.deeplink

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Bridge for iOS Swift code to send deep link URIs to the Kotlin deep link handler.
 *
 * Called from Swift AppDelegate:
 * ```swift
 * func application(_ app: UIApplication, open url: URL, options: ...) -> Bool {
 *     DeepLinkBridgeKt.handleDeepLinkUri(uri: url.absoluteString)
 *     return true
 * }
 * ```
 */
object DeepLinkBridge : KoinComponent {
    private val deepLinkHandler: DeepLinkHandler by inject()

    fun handleDeepLinkUri(uri: String) {
        deepLinkHandler.handleIncomingUri(uri)
    }
}
