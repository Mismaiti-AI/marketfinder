package com.marketfinder.core.data.notifications

import kotlinx.coroutines.flow.Flow

/**
 * Push Notification Service - Pre-Built Template Component
 *
 * Cross-platform interface for Firebase Cloud Messaging (FCM).
 * Platform implementations handle FCM token retrieval, permission
 * requests, and incoming message relay.
 *
 * Usage:
 * ```
 * val pushService: PushNotificationService = koinInject()
 *
 * // Request notification permission (required on Android 13+ and iOS)
 * val granted = pushService.requestPermission()
 *
 * // Get FCM token for server registration
 * val token = pushService.getFcmToken()
 *
 * // Observe incoming push messages
 * pushService.incomingMessages.collect { message ->
 *     println("Push: ${message.title} - ${message.body}")
 * }
 * ```
 *
 * Setup required:
 * - Android: Add google-services.json to composeApp/
 * - iOS: Add GoogleService-Info.plist to iosApp/iosApp/
 * - iOS: Add Firebase iOS SDK via SPM (FirebaseMessaging package)
 * - iOS: Enable Push Notifications + Background Modes in Xcode capabilities
 * - Upload APNs key (.p8) in Firebase Console
 */
interface PushNotificationService {
    suspend fun requestPermission(): Boolean
    suspend fun getFcmToken(): String?
    val incomingMessages: Flow<PushMessage>
}
