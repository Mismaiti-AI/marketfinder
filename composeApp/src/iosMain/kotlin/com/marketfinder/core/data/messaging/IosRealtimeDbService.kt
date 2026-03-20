package com.marketfinder.core.data.messaging

/**
 * iOS Realtime Database factory registration.
 *
 * The actual implementation is in Swift (iosApp/iosApp/util/firebase/RealtimeDbService.swift).
 * The Swift factory registers itself via [registerRealtimeDbServiceFactory] at app launch.
 */
private lateinit var registeredFactory: (() -> RealtimeDbService)

fun registerRealtimeDbServiceFactory(factory: () -> RealtimeDbService) {
    registeredFactory = factory
}

fun getRealtimeDbService(): RealtimeDbService {
    check(::registeredFactory.isInitialized) {
        "RealtimeDbService factory not registered. Call registerRealtimeDbServiceFactory() from Swift at app launch."
    }
    return registeredFactory.invoke()
}
