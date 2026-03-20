package com.marketfinder.core.data.firestore

import kotlinx.coroutines.flow.Flow

/**
 * Firestore Service - Pre-Built Template Component
 *
 * Cross-platform interface for Firebase Firestore.
 * Platform implementations use native Firebase SDKs:
 * - Android: com.google.firebase:firebase-firestore-ktx
 * - iOS: FirebaseFirestore Swift SDK (bridged via factory pattern)
 *
 * Generated repositories should inject this service and delegate
 * Firestore operations to it.
 *
 * Usage:
 * ```
 * class EventRepositoryImpl(
 *     private val firestore: FirestoreService
 * ) : EventRepository {
 *     override suspend fun loadEvents() {
 *         val events = firestore.getCollection("events")
 *         _events.value = events.map { it.toEvent() }
 *     }
 * }
 * ```
 *
 * Setup required:
 * - Android: Add google-services.json to composeApp/
 * - iOS: Add GoogleService-Info.plist to iosApp/iosApp/
 * - iOS: Add Firebase iOS SDK via SPM (FirebaseFirestore package)
 */
interface FirestoreService {
    /** Observe a collection in real-time. Returns a Flow of document maps. */
    fun observeCollection(collectionPath: String): Flow<List<Map<String, Any?>>>

    /** Observe a single document in real-time. */
    fun observeDocument(collectionPath: String, documentId: String): Flow<Map<String, Any?>?>

    /** Get all documents in a collection (one-shot). */
    suspend fun getCollection(collectionPath: String): List<Map<String, Any?>>

    /** Get a single document (one-shot). */
    suspend fun getDocument(collectionPath: String, documentId: String): Map<String, Any?>?

    /** Add a document (auto-generated ID). Returns the document ID. */
    suspend fun addDocument(collectionPath: String, data: Map<String, Any?>): String

    /** Set (create/overwrite) a document at a specific ID. */
    suspend fun setDocument(collectionPath: String, documentId: String, data: Map<String, Any?>)

    /** Update specific fields of a document. */
    suspend fun updateDocument(collectionPath: String, documentId: String, fields: Map<String, Any?>)

    /** Delete a document. */
    suspend fun deleteDocument(collectionPath: String, documentId: String)

    /** Query documents with simple filters. */
    suspend fun queryCollection(
        collectionPath: String,
        field: String,
        op: String,     // "==", "!=", "<", "<=", ">", ">=", "array-contains"
        value: Any
    ): List<Map<String, Any?>>
}
