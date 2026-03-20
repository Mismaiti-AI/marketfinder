package com.marketfinder.core.data.payment

import co.touchlab.kermit.Logger
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * RevenueCat Payment Service - Pre-Built Template Component
 *
 * Implementation of PaymentService using RevenueCat KMP SDK.
 * Wraps RevenueCat's cross-platform purchase and subscription APIs.
 */
class RevenueCatPaymentService(
    private val configService: PaymentConfigService
) : PaymentService {

    private val _customerInfo = MutableStateFlow<PaymentCustomerInfo?>(null)

    override suspend fun configure(appUserId: String?) {
        try {
            configService.refreshConfig()
            val apiKey = configService.getApiKey(getPlatformForApiKey())
            if (apiKey.isBlank()) {
                Logger.w("RevenueCat") { "No API key configured — skipping RevenueCat setup" }
                return
            }
            val config = PurchasesConfiguration(apiKey) {
                appUserId?.let { id -> this.appUserId = id }
            }
            Purchases.configure(config)
            refreshCustomerInfo()
            Logger.d("RevenueCat") { "Configured successfully" }
        } catch (e: Exception) {
            Logger.e("RevenueCat") { "Configuration failed: ${e.message}" }
        }
    }

    override suspend fun getOfferings(): List<PaymentOffering> {
        return try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val displayConfig = configService.config.value.offeringDisplay
            val productOrder = displayConfig.productOrder
            val productLabels = displayConfig.productLabels

            offerings.all.values.map { offering ->
                val products = offering.availablePackages.map { pkg ->
                    val product = pkg.storeProduct.toPaymentProduct()
                    // Check labels by both package identifier and product ID
                    val hasLabel = productLabels.containsKey(pkg.identifier) ||
                        productLabels.containsKey(product.id)
                    if (hasLabel) product.copy(isBestValue = true) else product
                }

                val sortedProducts = if (productOrder.isNotEmpty()) {
                    products.sortedBy { product ->
                        val index = productOrder.indexOf(product.id)
                        if (index >= 0) index else Int.MAX_VALUE
                    }
                } else {
                    products
                }

                PaymentOffering(
                    id = offering.identifier,
                    products = sortedProducts
                )
            }
        } catch (e: Exception) {
            Logger.e("RevenueCat") { "Get offerings failed: ${e.message}" }
            emptyList()
        }
    }

    override suspend fun purchase(packageId: String): PurchaseResult {
        return try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val pkg = offerings.all.values
                .flatMap { it.availablePackages }
                .firstOrNull { it.identifier == packageId }
                ?: return PurchaseResult.Error("Package not found: $packageId")

            val result = Purchases.sharedInstance.awaitPurchase(pkg)
            refreshCustomerInfo()

            val activeEntitlements = result.customerInfo.entitlements.active
            val entitlementId = activeEntitlements.keys.firstOrNull() ?: packageId
            PurchaseResult.Success(entitlementId)
        } catch (e: Exception) {
            val message = e.message ?: "Unknown error"
            if (message.contains("cancel", ignoreCase = true) ||
                message.contains("user cancelled", ignoreCase = true)
            ) {
                PurchaseResult.Cancelled
            } else {
                Logger.e("RevenueCat") { "Purchase failed: $message" }
                PurchaseResult.Error(message)
            }
        }
    }

    override suspend fun restorePurchases(): PurchaseResult {
        return try {
            val info = Purchases.sharedInstance.awaitRestore()
            refreshCustomerInfo()

            val active = info.entitlements.active
            if (active.isNotEmpty()) {
                PurchaseResult.Success(active.keys.first())
            } else {
                PurchaseResult.Error("No purchases to restore")
            }
        } catch (e: Exception) {
            Logger.e("RevenueCat") { "Restore failed: ${e.message}" }
            PurchaseResult.Error(e.message ?: "Restore failed")
        }
    }

    override fun observeCustomerInfo(): StateFlow<PaymentCustomerInfo?> = _customerInfo.asStateFlow()

    override suspend fun isEntitled(entitlementId: String): Boolean {
        return try {
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            info.entitlements.active.containsKey(entitlementId)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSubscriptionStatus(entitlementId: String): SubscriptionStatus {
        return try {
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            val entitlement = info.entitlements.all[entitlementId]
                ?: return SubscriptionStatus.None

            if (entitlement.isActive) {
                SubscriptionStatus.Active(
                    entitlementId = entitlementId,
                    expiresAtMillis = entitlement.expirationDateMillis,
                    willRenew = entitlement.willRenew
                )
            } else {
                SubscriptionStatus.Expired(
                    entitlementId = entitlementId,
                    expiredAtMillis = entitlement.expirationDateMillis ?: 0L
                )
            }
        } catch (e: Exception) {
            SubscriptionStatus.None
        }
    }

    override suspend fun logOut() {
        try {
            Purchases.sharedInstance.awaitLogOut()
            _customerInfo.value = null
        } catch (e: Exception) {
            Logger.e("RevenueCat") { "Logout failed: ${e.message}" }
        }
    }

    private suspend fun refreshCustomerInfo() {
        try {
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            _customerInfo.value = PaymentCustomerInfo(
                activeEntitlements = info.entitlements.active.keys,
                allPurchasedProductIds = info.allPurchasedProductIdentifiers,
                latestExpirationMillis = info.latestExpirationDateMillis
            )
        } catch (e: Exception) {
            Logger.e("RevenueCat") { "Refresh customer info failed: ${e.message}" }
        }
    }

    private fun StoreProduct.toPaymentProduct(): PaymentProduct {
        return PaymentProduct(
            id = id,
            title = title,
            description = localizedDescription ?: "",
            price = price.formatted,
            period = period?.toSubscriptionPeriod()
        )
    }

    private fun Period.toSubscriptionPeriod(): SubscriptionPeriod? {
        return when (unit) {
            PeriodUnit.YEAR -> SubscriptionPeriod.ANNUAL
            PeriodUnit.MONTH -> when {
                value >= 6 -> SubscriptionPeriod.SEMI_ANNUAL
                value >= 3 -> SubscriptionPeriod.QUARTERLY
                else -> SubscriptionPeriod.MONTHLY
            }
            PeriodUnit.WEEK -> SubscriptionPeriod.WEEKLY
            PeriodUnit.DAY -> if (value >= 7) SubscriptionPeriod.WEEKLY else null
            PeriodUnit.UNKNOWN -> null
        }
    }
}

/** Returns "android" or "ios" based on the current platform. */
internal expect fun getPlatformForApiKey(): String
