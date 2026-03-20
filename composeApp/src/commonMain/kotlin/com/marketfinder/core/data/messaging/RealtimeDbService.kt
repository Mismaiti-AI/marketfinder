package com.marketfinder.core.data.messaging

import kotlinx.coroutines.flow.Flow

/**
 * Realtime Database Service - Pre-Built Template Component
 *
 * Cross-platform interface for Firebase Realtime Database.
 * Platform implementations use native Firebase SDKs:
 * - Android: com.google.firebase.database.FirebaseDatabase
 * - iOS: FirebaseDatabase Swift SDK (bridged via factory pattern)
 *
 * Usage:
 * ```
 * class ChatRepositoryImpl(
 *     private val realtimeDb: RealtimeDbService
 * ) : ChatRepository {
 *     override fun observeMessages(conversationId: String) =
 *         realtimeDb.observeMessages(conversationId)
 *
 *     override suspend fun sendMessage(conversationId: String, message: ChatMessage) {
 *         realtimeDb.sendMessage(conversationId, message)
 *     }
 * }
 * ```
 *
 * Setup required:
 * - Android: Add google-services.json to composeApp/
 * - iOS: Add GoogleService-Info.plist to iosApp/iosApp/
 * - iOS: Add Firebase iOS SDK via SPM (FirebaseDatabase package)
 * - Enable Realtime Database in Firebase Console
 */
interface RealtimeDbService {

    /** Observe messages in a conversation in real-time. */
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>

    /** Send a message to a conversation. Returns the message ID. */
    suspend fun sendMessage(conversationId: String, message: ChatMessage): String

    /** Observe all conversations for a user in real-time. */
    fun observeConversations(userId: String): Flow<List<Conversation>>

    /** Mark a message as read. */
    suspend fun markAsRead(conversationId: String, messageId: String)

    /** Set typing status for a user in a conversation. */
    suspend fun setTypingStatus(conversationId: String, userId: String, isTyping: Boolean)

    /** Observe typing statuses in a conversation. */
    fun observeTyping(conversationId: String): Flow<List<TypingStatus>>

    /** Delete a message from a conversation. */
    suspend fun deleteMessage(conversationId: String, messageId: String)
}
