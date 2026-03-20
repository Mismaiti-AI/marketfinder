package com.marketfinder.core.data.messaging

/**
 * Messaging Models - Pre-Built Template Component
 *
 * Data classes for real-time messaging using Firebase Realtime Database.
 */

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestampMillis: Long,
    val isRead: Boolean = false,
    val type: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT, IMAGE, SYSTEM
}

data class Conversation(
    val id: String,
    val participants: List<String>,
    val lastMessage: String? = null,
    val lastSenderName: String? = null,
    val unreadCount: Int = 0,
    val updatedAtMillis: Long = 0L
)

data class TypingStatus(
    val userId: String,
    val isTyping: Boolean
)
