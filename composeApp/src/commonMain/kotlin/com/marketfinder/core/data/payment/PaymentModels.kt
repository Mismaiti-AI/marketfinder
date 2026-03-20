package com.marketfinder.core.data.payment

/**
 * Payment Models - Pre-Built Template Component
 *
 * Data classes for payment/subscription functionality using RevenueCat.
 */

data class PaymentProduct(
    val id: String,
    val title: String,
    val description: String = "",
    val price: String,
    val period: SubscriptionPeriod? = null,
    val isBestValue: Boolean = false
)

enum class SubscriptionPeriod(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    SEMI_ANNUAL("6 Months"),
    ANNUAL("Annual"),
    LIFETIME("Lifetime")
}

sealed interface SubscriptionStatus {
    data class Active(
        val entitlementId: String,
        val expiresAtMillis: Long?,
        val willRenew: Boolean = true
    ) : SubscriptionStatus

    data class Expired(
        val entitlementId: String,
        val expiredAtMillis: Long
    ) : SubscriptionStatus

    data class Trial(
        val entitlementId: String,
        val expiresAtMillis: Long
    ) : SubscriptionStatus

    data object None : SubscriptionStatus
}

sealed interface PurchaseResult {
    data class Success(val entitlementId: String) : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Error(val message: String) : PurchaseResult
}

data class PaymentCustomerInfo(
    val activeEntitlements: Set<String>,
    val allPurchasedProductIds: Set<String>,
    val latestExpirationMillis: Long?
)

data class PaymentOffering(
    val id: String,
    val products: List<PaymentProduct>
)
