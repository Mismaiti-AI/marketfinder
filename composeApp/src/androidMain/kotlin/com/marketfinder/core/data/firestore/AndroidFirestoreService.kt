package com.marketfinder.core.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AndroidFirestoreService : FirestoreService {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun observeCollection(collectionPath: String): Flow<List<Map<String, Any?>>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection(collectionPath)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val docs = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                } ?: emptyList()
                trySend(docs)
            }
        awaitClose { registration.remove() }
    }

    override fun observeDocument(collectionPath: String, documentId: String): Flow<Map<String, Any?>?> = callbackFlow {
        val registration = firestore.collection(collectionPath).document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.data?.plus("id" to snapshot.id))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getCollection(collectionPath: String): List<Map<String, Any?>> {
        val snapshot = firestore.collection(collectionPath).get().await()
        return snapshot.documents.mapNotNull { doc -> doc.data?.plus("id" to doc.id) }
    }

    override suspend fun getDocument(collectionPath: String, documentId: String): Map<String, Any?>? {
        val snapshot = firestore.collection(collectionPath).document(documentId).get().await()
        return snapshot.data?.plus("id" to snapshot.id)
    }

    override suspend fun addDocument(collectionPath: String, data: Map<String, Any?>): String {
        val docRef = firestore.collection(collectionPath).add(data).await()
        return docRef.id
    }

    override suspend fun setDocument(collectionPath: String, documentId: String, data: Map<String, Any?>) {
        firestore.collection(collectionPath).document(documentId).set(data).await()
    }

    override suspend fun updateDocument(collectionPath: String, documentId: String, fields: Map<String, Any?>) {
        firestore.collection(collectionPath).document(documentId).update(fields).await()
    }

    override suspend fun deleteDocument(collectionPath: String, documentId: String) {
        firestore.collection(collectionPath).document(documentId).delete().await()
    }

    override suspend fun queryCollection(
        collectionPath: String, field: String, op: String, value: Any
    ): List<Map<String, Any?>> {
        val query = firestore.collection(collectionPath)
        val typedQuery = when (op) {
            "==" -> query.whereEqualTo(field, value)
            "!=" -> query.whereNotEqualTo(field, value)
            "<" -> query.whereLessThan(field, value)
            "<=" -> query.whereLessThanOrEqualTo(field, value)
            ">" -> query.whereGreaterThan(field, value)
            ">=" -> query.whereGreaterThanOrEqualTo(field, value)
            "array-contains" -> query.whereArrayContains(field, value)
            else -> query
        }
        val snapshot = typedQuery.get().await()
        return snapshot.documents.mapNotNull { doc -> doc.data?.plus("id" to doc.id) }
    }
}
