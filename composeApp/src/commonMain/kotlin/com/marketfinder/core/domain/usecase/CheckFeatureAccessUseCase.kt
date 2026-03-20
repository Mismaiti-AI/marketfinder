package com.marketfinder.core.domain.usecase

import com.marketfinder.core.data.payment.PaymentConfigService
import com.marketfinder.core.data.payment.PaymentService

class CheckFeatureAccessUseCase(
    private val configService: PaymentConfigService,
    private val paymentService: PaymentService
) {
    suspend operator fun invoke(featureId: String): Boolean {
        val entitlements = paymentService.observeCustomerInfo().value?.activeEntitlements ?: emptySet()
        return configService.isFeatureEnabled(featureId, entitlements)
    }
}
