package com.marketfinder.core.data.auth

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth

/**
 * Firebase Auth implementation of [SocialAuthBackendHandler].
 *
 * Funnels platform-native sign-in tokens (Google on Android, Apple on iOS)
 * through Firebase Auth via `signInWithCredential()`, producing a single
 * Firebase UID per user regardless of platform.
 *
 * ## Flow
 * ```
 * Android: Google Sign-In → idToken → GoogleAuthProvider.credential() → Firebase UID
 * iOS:     Apple Sign-In  → idToken + rawNonce → OAuthProvider.credential("apple.com") → Firebase UID
 * ```
 *
 * ## Guest Migration
 * If the current user is anonymous (guest mode), `linkWithCredential` is used
 * instead of `signInWithCredential`. This preserves the guest's Firebase UID
 * and all associated Firestore data — no data loss on registration.
 *
 * ## Setup
 * Register in Koin:
 * ```kotlin
 * single<SocialAuthBackendHandler> { FirebaseAuthHandler() }
 * ```
 */
class FirebaseAuthHandler : SocialAuthBackendHandler {

    override suspend fun authenticate(result: SocialAuthResult): BackendAuthResponse {
        val credential = when (result.provider) {
            AuthProvider.GOOGLE -> {
                val idToken = result.idToken
                    ?: error("Google Sign-In missing idToken")
                GoogleAuthProvider.credential(idToken, null)
            }
            AuthProvider.APPLE -> {
                val idToken = result.idToken
                    ?: error("Apple Sign-In missing identityToken")
                val rawNonce = result.rawNonce
                    ?: error("Apple Sign-In missing rawNonce (required for Firebase)")
                OAuthProvider.credential("apple.com", idToken, rawNonce)
            }
            else -> error("Unsupported auth provider: ${result.provider}")
        }

        val currentUser = Firebase.auth.currentUser
        val authResult = if (currentUser != null && currentUser.isAnonymous) {
            // Guest → registered: link preserves UID + all Firestore data
            Logger.i("FirebaseAuthHandler") { "Linking anonymous user with ${result.provider}" }
            currentUser.linkWithCredential(credential)
        } else {
            Logger.i("FirebaseAuthHandler") { "Signing in with ${result.provider}" }
            Firebase.auth.signInWithCredential(credential)
        }

        val user = authResult.user
            ?: error("Firebase auth succeeded but user is null")

        return BackendAuthResponse(
            userId = user.uid,
            name = user.displayName ?: result.name,
            email = user.email ?: result.email
        )
    }
}
