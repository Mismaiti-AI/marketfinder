package com.marketfinder.core.data.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Default implementation of [DeepLinkHandler].
 *
 * Uses a MutableStateFlow to hold the pending deep link URI.
 * Thread-safe for concurrent access from platform callbacks.
 */
class DefaultDeepLinkHandler : DeepLinkHandler {
    private val _pendingDeepLink = MutableStateFlow<String?>(null)
    override val pendingDeepLink: StateFlow<String?> = _pendingDeepLink.asStateFlow()

    override fun handleIncomingUri(uri: String) {
        _pendingDeepLink.value = uri
    }

    override fun consumeDeepLink() {
        _pendingDeepLink.value = null
    }
}
