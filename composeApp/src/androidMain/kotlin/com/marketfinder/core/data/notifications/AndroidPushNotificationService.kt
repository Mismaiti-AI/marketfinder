package com.marketfinder.core.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await

/**
 * Android implementation of [PushNotificationService].
 *
 * Uses Firebase Cloud Messaging for token retrieval and message relay.
 * Creates a default notification channel on init.
 */
class AndroidPushNotificationService(
    private val context: Context
) : PushNotificationService {

    private val _incomingMessages = MutableSharedFlow<PushMessage>(extraBufferCapacity = 64)
    override val incomingMessages: Flow<PushMessage> = _incomingMessages.asSharedFlow()

    init {
        createDefaultNotificationChannel()
    }

    override suspend fun requestPermission(): Boolean {
        // POST_NOTIFICATIONS permission is only required on Android 13+
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            null
        }
    }

    internal fun emitMessage(message: PushMessage) {
        _incomingMessages.tryEmit(message)
    }

    private fun createDefaultNotificationChannel() {
        val channel = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "General",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "General push notifications"
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val DEFAULT_CHANNEL_ID = "mismaiti_default"

        // Shared instance for MismaitiFcmService to emit messages
        internal var instance: AndroidPushNotificationService? = null
    }
}
