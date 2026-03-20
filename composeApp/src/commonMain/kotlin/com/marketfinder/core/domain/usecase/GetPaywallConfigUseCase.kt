package com.marketfinder.core.domain.usecase

import com.marketfinder.core.data.payment.PaymentConfigService
import com.marketfinder.core.data.payment.PaywallConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPaywallConfigUseCase(private val configService: PaymentConfigService) {

    operator fun invoke(): Flow<PaywallConfig> {
        return configService.config.map { it.paywall }
    }

    fun currentValue(): PaywallConfig {
        return configService.config.value.paywall
    }

    suspend fun refresh() {
        configService.refreshConfig()
    }
}
