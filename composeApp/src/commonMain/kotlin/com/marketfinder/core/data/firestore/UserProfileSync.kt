package com.marketfinder.core.data.firestore

import co.touchlab.kermit.Logger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Syncs user profile data to Firestore `users` collection after sign-in.
 *
 * This makes app users visible in the admin panel. Each sign-in writes:
 * - name, email, authProvider, lastSignInAt
 * - createdAt (only on first sign-in, preserved on subsequent ones)
 *
 * The Firebase Auth UID is used as the document ID, ensuring one document per user.
 *
 * Requires: [firestore] feature flag enabled.
 */
class UserProfileSync(private val firestoreService: FirestoreService) {

    @OptIn(ExperimentalTime::class)
    suspend fun sync(uid: String, name: String, email: String, provider: String) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()

            // Check if user doc already exists to preserve createdAt
            val existing = firestoreService.getDocument("users", uid)

            val data = mutableMapOf<String, Any?>(
                "name" to name,
                "email" to email,
                "authProvider" to provider,
                "lastSignInAt" to now,
            )

            if (existing == null) {
                // First sign-in — set createdAt
                data["createdAt"] = now
                Logger.i("UserProfileSync") { "Creating user profile for $email (uid=$uid)" }
            } else {
                // Preserve original createdAt
                data["createdAt"] = existing["createdAt"] ?: now
                Logger.i("UserProfileSync") { "Updating user profile for $email (uid=$uid)" }
            }

            firestoreService.setDocument("users", uid, data)
            Logger.i("UserProfileSync") { "User profile synced to Firestore" }
        } catch (e: Exception) {
            // Non-fatal — don't block sign-in if Firestore sync fails
            Logger.w("UserProfileSync") { "Failed to sync user profile: ${e.message}" }
        }
    }
}
