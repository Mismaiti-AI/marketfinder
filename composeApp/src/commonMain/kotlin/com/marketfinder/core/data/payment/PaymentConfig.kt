package com.marketfinder.core.data.payment

data class PaymentConfig(
    val revenueCatApiKey: PlatformApiKey = PlatformApiKey(),
    val paywall: PaywallConfig = PaywallConfig(),
    val entitlementMapping: Map<String, List<String>> = emptyMap(),
    val offeringDisplay: OfferingDisplayConfig = OfferingDisplayConfig()
)

data class PlatformApiKey(
    val android: String = "",
    val ios: String = ""
)

data class PaywallConfig(
    val title: String = "Upgrade to Pro",
    val subtitle: String = "Unlock all features",
    val features: List<String> = emptyList(),
    val ctaText: String = "Subscribe Now",
    val restoreText: String = "Restore Purchases"
)

data class OfferingDisplayConfig(
    val defaultOfferingId: String = "default",
    val productOrder: List<String> = emptyList(),
    val productLabels: Map<String, String> = emptyMap()
)

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toPaymentConfig(): PaymentConfig {
    val apiKeyMap = (this["revenueCatApiKey"] as? Map<String, Any?>) ?: emptyMap()
    val paywallMap = (this["paywall"] as? Map<String, Any?>) ?: emptyMap()
    val entitlementMap = (this["entitlementMapping"] as? Map<String, Any?>) ?: emptyMap()
    val offeringMap = (this["offeringDisplay"] as? Map<String, Any?>) ?: emptyMap()

    return PaymentConfig(
        revenueCatApiKey = PlatformApiKey(
            android = apiKeyMap["android"] as? String ?: "",
            ios = apiKeyMap["ios"] as? String ?: ""
        ),
        paywall = PaywallConfig(
            title = paywallMap["title"] as? String ?: "Upgrade to Pro",
            subtitle = paywallMap["subtitle"] as? String ?: "Unlock all features",
            features = (paywallMap["features"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            ctaText = paywallMap["ctaText"] as? String ?: "Subscribe Now",
            restoreText = paywallMap["restoreText"] as? String ?: "Restore Purchases"
        ),
        entitlementMapping = entitlementMap.mapValues { (_, value) ->
            (value as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        },
        offeringDisplay = OfferingDisplayConfig(
            defaultOfferingId = offeringMap["defaultOfferingId"] as? String ?: "default",
            productOrder = (offeringMap["productOrder"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            productLabels = (offeringMap["productLabels"] as? Map<String, Any?>)
                ?.mapValues { it.value as? String ?: "" } ?: emptyMap()
        )
    )
}
