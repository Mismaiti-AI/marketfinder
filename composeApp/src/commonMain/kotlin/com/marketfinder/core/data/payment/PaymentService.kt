package com.marketfinder.core.data.payment

import kotlinx.coroutines.flow.StateFlow

/**
 * Payment Service - Pre-Built Template Component
 *
 * Cross-platform interface for in-app purchases and subscriptions.
 * Default implementation uses RevenueCat KMP SDK.
 *
 * Usage:
 * ```
 * class SubscriptionViewModel(
 *     private val paymentService: PaymentService
 * ) : BaseViewModel() {
 *     init {
 *         safeLaunch {
 *             val offerings = paymentService.getOfferings()
 *             // Display products to user
 *         }
 *     }
 *
 *     fun purchase(packageId: String) = safeLaunch {
 *         when (val result = paymentService.purchase(packageId)) {
 *             is PurchaseResult.Success -> // Handle success
 *             is PurchaseResult.Cancelled -> // User cancelled
 *             is PurchaseResult.Error -> // Handle error
 *         }
 *     }
 * }
 * ```
 *
 * Setup required:
 * - Configure RevenueCat API keys via the web admin Payment Config page
 * - Configure products in RevenueCat dashboard
 * - Set up App Store / Play Store products
 */
interface PaymentService {

    /** Configure the payment SDK. Call once at app startup. API key is read from Firestore config. */
    suspend fun configure(appUserId: String? = null)

    /** Get available product offerings. */
    suspend fun getOfferings(): List<PaymentOffering>

    /** Purchase a product by its package ID. */
    suspend fun purchase(packageId: String): PurchaseResult

    /** Restore previously purchased products. */
    suspend fun restorePurchases(): PurchaseResult

    /** Observe customer info changes (entitlements, subscriptions). */
    fun observeCustomerInfo(): StateFlow<PaymentCustomerInfo?>

    /** Check if user has a specific entitlement. */
    suspend fun isEntitled(entitlementId: String): Boolean

    /** Get current subscription status for an entitlement. */
    suspend fun getSubscriptionStatus(entitlementId: String): SubscriptionStatus

    /** Log out current user (for user-switching scenarios). */
    suspend fun logOut()
}
