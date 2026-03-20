package com.marketfinder.core.data.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

// CommonCrypto SHA256 via cinterop
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.allocArray
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

/**
 * Apple Sign-In implementation using AuthenticationServices framework.
 *
 * ## Setup Required
 * 1. Open `iosApp.xcodeproj` in Xcode
 * 2. Go to target -> Signing & Capabilities -> Add "Sign in with Apple"
 * 3. Ensure your Apple Developer account has this capability enabled
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun signInWithSocialProvider(): SocialAuthResult {
    val deferred = CompletableDeferred<SocialAuthResult>()

    val rawNonce = generateRandomNonce()
    val hashedNonce = sha256(rawNonce)

    val appleIDProvider = ASAuthorizationAppleIDProvider()
    val request = appleIDProvider.createRequest().apply {
        requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
        nonce = hashedNonce
    }

    val delegate = AppleSignInDelegate(deferred, rawNonce)
    val controller = ASAuthorizationController(authorizationRequests = listOf(request))
    controller.delegate = delegate
    controller.presentationContextProvider = delegate
    controller.performRequests()

    return deferred.await()
}

/**
 * Generate a cryptographically secure random nonce string.
 * Uses SecRandomCopyBytes (iOS Security framework).
 */
@OptIn(ExperimentalForeignApi::class)
private fun generateRandomNonce(length: Int = 32): String {
    val charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._"
    val randomBytes = ByteArray(length)
    randomBytes.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, length.convert(), pinned.addressOf(0))
    }
    return randomBytes.map { byte ->
        charset[(byte.toInt() and 0xFF) % charset.length]
    }.joinToString("")
}

/**
 * SHA256 hash using CommonCrypto (CC_SHA256).
 * Returns hex-encoded digest string.
 */
@OptIn(ExperimentalForeignApi::class)
private fun sha256(input: String): String {
    val data = input.encodeToByteArray()
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)

    data.usePinned { dataPinned ->
        digest.usePinned { digestPinned ->
            CC_SHA256(dataPinned.addressOf(0), data.size.convert(), digestPinned.addressOf(0))
        }
    }

    return digest.joinToString("") {
        it.toString(16).padStart(2, '0')
    }
}

@OptIn(ExperimentalForeignApi::class)
private class AppleSignInDelegate(
    private val deferred: CompletableDeferred<SocialAuthResult>,
    private val rawNonce: String
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {

    @OptIn(BetaInteropApi::class)
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        if (credential == null) {
            deferred.completeExceptionally(Exception("Apple Sign-In failed: invalid credential type"))
            return
        }

        val userId = credential.user
        val fullName = credential.fullName
        val name = buildString {
            fullName?.givenName?.let { append(it) }
            fullName?.familyName?.let {
                if (isNotEmpty()) append(" ")
                append(it)
            }
        }
        val email = credential.email ?: ""

        val identityToken = credential.identityToken?.let {
            platform.Foundation.NSString.create(
                data = it,
                encoding = platform.Foundation.NSUTF8StringEncoding
            )?.toString()
        }

        deferred.complete(
            SocialAuthResult(
                id = userId,
                name = name,
                email = email,
                provider = AuthProvider.APPLE,
                idToken = identityToken,
                rawNonce = rawNonce
            )
        )
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        deferred.completeExceptionally(
            Exception("Apple Sign-In failed: ${didCompleteWithError.localizedDescription}")
        )
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun presentationAnchorForAuthorizationController(
        controller: ASAuthorizationController
    ): UIWindow {
        val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        return scene?.windows?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
            ?: UIWindow()
    }
}
