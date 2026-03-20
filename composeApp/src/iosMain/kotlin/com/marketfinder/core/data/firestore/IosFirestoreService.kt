package com.marketfinder.core.data.firestore

/**
 * iOS Firestore factory registration.
 *
 * The actual implementation is in Swift (iosApp/iosApp/util/firebase/FirestoreService.swift).
 * The Swift factory registers itself via [registerFirestoreServiceFactory] at app launch.
 */
private lateinit var registeredFactory: (() -> FirestoreService)

fun registerFirestoreServiceFactory(factory: () -> FirestoreService) {
    registeredFactory = factory
}

fun getFirestoreService(): FirestoreService {
    return registeredFactory.invoke()
}
