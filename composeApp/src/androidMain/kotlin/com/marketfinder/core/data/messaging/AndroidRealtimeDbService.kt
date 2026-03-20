package com.marketfinder.core.data.messaging

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Android Realtime Database Service - Pre-Built Template Component
 *
 * Android implementation using Firebase Realtime Database SDK.
 * Follows the same callbackFlow + awaitClose pattern as AndroidFirestoreService.
 */
class AndroidRealtimeDbService : RealtimeDbService {

    private val database by lazy { FirebaseDatabase.getInstance() }

    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val query = database.getReference("messages/$conversationId")
            .orderByChild("timestampMillis")

        val listener = query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updated = snapshot.children.mapNotNull { child ->
                        child.toChatMessage(conversationId)
                    }
                    trySend(updated)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        awaitClose { query.removeEventListener(listener) }
    }

    override suspend fun sendMessage(conversationId: String, message: ChatMessage): String {
        val ref = database.getReference("messages/$conversationId")
        val newRef = ref.push()
        val messageData = mapOf(
            "senderId" to message.senderId,
            "senderName" to message.senderName,
            "content" to message.content,
            "timestampMillis" to message.timestampMillis,
            "isRead" to message.isRead,
            "type" to message.type.name
        )
        newRef.setValue(messageData).await()

        // Update conversation last message
        val convRef = database.getReference("conversations/$conversationId")
        convRef.updateChildren(
            mapOf(
                "lastMessage" to message.content,
                "lastSenderName" to message.senderName,
                "updatedAtMillis" to message.timestampMillis
            )
        ).await()

        return newRef.key ?: ""
    }

    override fun observeConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val ref = database.getReference("user_conversations/$userId")

        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = snapshot.children.mapNotNull { child ->
                    child.toConversation()
                }
                trySend(conversations)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun markAsRead(conversationId: String, messageId: String) {
        database.getReference("messages/$conversationId/$messageId/isRead")
            .setValue(true)
            .await()
    }

    override suspend fun setTypingStatus(conversationId: String, userId: String, isTyping: Boolean) {
        val ref = database.getReference("typing/$conversationId/$userId")
        if (isTyping) {
            ref.setValue(true).await()
            ref.onDisconnect().removeValue().await()
        } else {
            ref.removeValue().await()
        }
    }

    override fun observeTyping(conversationId: String): Flow<List<TypingStatus>> = callbackFlow {
        val ref = database.getReference("typing/$conversationId")

        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val statuses = snapshot.children.mapNotNull { child ->
                    val userId = child.key ?: return@mapNotNull null
                    val typing = child.getValue(Boolean::class.java) ?: false
                    TypingStatus(userId = userId, isTyping = typing)
                }
                trySend(statuses)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })

        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun deleteMessage(conversationId: String, messageId: String) {
        database.getReference("messages/$conversationId/$messageId")
            .removeValue()
            .await()
    }

    private fun DataSnapshot.toChatMessage(conversationId: String): ChatMessage? {
        val id = key ?: return null
        return ChatMessage(
            id = id,
            conversationId = conversationId,
            senderId = child("senderId").getValue(String::class.java) ?: return null,
            senderName = child("senderName").getValue(String::class.java) ?: "",
            content = child("content").getValue(String::class.java) ?: "",
            timestampMillis = child("timestampMillis").getValue(Long::class.java) ?: 0L,
            isRead = child("isRead").getValue(Boolean::class.java) ?: false,
            type = try {
                MessageType.valueOf(child("type").getValue(String::class.java) ?: "TEXT")
            } catch (e: Exception) {
                MessageType.TEXT
            }
        )
    }

    private fun DataSnapshot.toConversation(): Conversation? {
        val id = key ?: return null
        return Conversation(
            id = id,
            participants = child("participants").children.mapNotNull {
                it.getValue(String::class.java)
            },
            lastMessage = child("lastMessage").getValue(String::class.java),
            lastSenderName = child("lastSenderName").getValue(String::class.java),
            unreadCount = child("unreadCount").getValue(Int::class.java) ?: 0,
            updatedAtMillis = child("updatedAtMillis").getValue(Long::class.java) ?: 0L
        )
    }
}
