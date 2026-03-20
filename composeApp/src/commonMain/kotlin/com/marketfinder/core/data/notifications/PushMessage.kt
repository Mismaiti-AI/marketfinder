package com.marketfinder.core.data.notifications

/**
 * Push Message - Pre-Built Template Component
 *
 * Represents an incoming push notification message from FCM.
 *
 * @param messageId Unique message identifier from FCM
 * @param title Notification title (may be null for data-only messages)
 * @param body Notification body text (may be null for data-only messages)
 * @param data Custom key-value payload from the push message
 */
data class PushMessage(
    val messageId: String,
    val title: String? = null,
    val body: String? = null,
    val data: Map<String, String> = emptyMap()
)
