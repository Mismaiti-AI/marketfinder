package com.marketfinder.core.di

import co.touchlab.kermit.Logger
// [auth] start
// import com.marketfinder.core.data.auth.AuthRepository
// import com.marketfinder.core.presentation.auth.AuthViewModel
// [auth] end
// [firebase_auth] start
// import com.marketfinder.core.data.auth.FirebaseAuthHandler
// import com.marketfinder.core.data.auth.SocialAuthBackendHandler
// [firebase_auth] end
// [firestore] start
// import com.marketfinder.core.data.firestore.UserProfileSync
// [firestore] end
// [google_sheets] start
// import com.marketfinder.core.data.gsheets.GoogleSheetsConfig
// import com.marketfinder.core.data.gsheets.GoogleSheetsService
// [google_sheets] end
// [chat] start
// import com.marketfinder.core.data.chat.AiChatService
// [chat] end
// [payment] start
// import com.marketfinder.core.data.payment.PaymentConfigService
// import com.marketfinder.core.data.payment.PaymentService
// import com.marketfinder.core.data.payment.RevenueCatPaymentService
// import com.marketfinder.core.domain.usecase.GetPaywallConfigUseCase
// import com.marketfinder.core.domain.usecase.CheckFeatureAccessUseCase
// [payment] end
// [messaging] Note: RealtimeDbService is registered in platform modules (Android/iOS)
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect fun platformModule() : Module

fun coreModule() = module {

    factory {
        HttpClient(get()) {
            install(HttpTimeout) {
                socketTimeoutMillis = 120_000  // 2 minutes
                requestTimeoutMillis = 120_000 // 2 minutes
                connectTimeoutMillis = 60_000  // 60 seconds
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 1)
                exponentialDelay()
                modifyRequest { request ->
                    request.headers.append("x-retry-count", retryCount.toString())
                }
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        Logger.d("ktor client external") {
                            message
                        }
                    }
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // Don't automatically throw exceptions for HTTP error status codes
            // This allows our BaseService.handleResponse() to handle errors properly
            expectSuccess = false
        }
    }

    // [firestore] start
    // single { UserProfileSync(firestoreService = get()) }
    // [firestore] end

    // [auth] start
    // single { AuthRepository(database = get(), backendHandler = getOrNull(), userProfileSync = getOrNull()) }
    // viewModelOf(::AuthViewModel)
    // [auth] end

    // [firebase_auth] start
    // single<SocialAuthBackendHandler> { FirebaseAuthHandler() }
    // [firebase_auth] end

    // [google_sheets] start
    // single { GoogleSheetsConfig(get()) }
    // single { GoogleSheetsService(get()) }
    // [google_sheets] end

    // [chat] start
    // Note: Register your AiChatService implementation here
    // single<AiChatService> { YourChatServiceImpl(get()) }
    // [chat] end

    // [payment] start
    // single { PaymentConfigService(get(), get()) }
    // single<PaymentService> { RevenueCatPaymentService(get()) }
    // factory { GetPaywallConfigUseCase(get()) }
    // factory { CheckFeatureAccessUseCase(get(), get()) }
    // [payment] end

}
