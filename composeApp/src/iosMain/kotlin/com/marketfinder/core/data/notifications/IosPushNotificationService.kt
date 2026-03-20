package com.marketfinder.core.data.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of [PushNotificationService].
 *
 * Uses UNUserNotificationCenter for permission requests.
 * FCM token is set from Swift AppDelegate via the companion object.
 * Incoming messages are relayed via the companion's emitMessage().
 */
class IosPushNotificationService : PushNotificationService {

    private val _incomingMessages = MutableSharedFlow<PushMessage>(extraBufferCapacity = 64)
    override val incomingMessages: Flow<PushMessage> = _incomingMessages.asSharedFlow()

    init {
        instance = this
    }

    override suspend fun requestPermission(): Boolean = suspendCoroutine { continuation ->
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        ) { granted, _ ->
            continuation.resume(granted)
        }
    }

    override suspend fun getFcmToken(): String? = fcmToken

    internal fun emitMessage(message: PushMessage) {
        _incomingMessages.tryEmit(message)
    }

    companion object {
        internal var instance: IosPushNotificationService? = null

        /**
         * Set from Swift AppDelegate when FCM token is received.
         */
        var fcmToken: String? = null

        /**
         * Called from Swift AppDelegate when a push notification is received.
         */
        fun onMessageReceived(
            messageId: String,
            title: String?,
            body: String?,
            data: Map<String, String>
        ) {
            instance?.emitMessage(
                PushMessage(
                    messageId = messageId,
                    title = title,
                    body = body,
                    data = data
                )
            )
        }
    }
}
