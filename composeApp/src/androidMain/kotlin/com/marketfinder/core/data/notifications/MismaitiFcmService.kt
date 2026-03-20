package com.marketfinder.core.data.notifications

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for receiving push notifications.
 *
 * Relays incoming messages to [AndroidPushNotificationService] via shared flow.
 * Must be declared in AndroidManifest.xml as a service.
 */
class MismaitiFcmService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val pushMessage = PushMessage(
            messageId = remoteMessage.messageId ?: "",
            title = remoteMessage.notification?.title,
            body = remoteMessage.notification?.body,
            data = remoteMessage.data
        )

        Logger.d("MismaitiFcmService") { "Message received: ${pushMessage.title}" }

        AndroidPushNotificationService.instance?.emitMessage(pushMessage)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.d("MismaitiFcmService") { "New FCM token: $token" }
    }
}
