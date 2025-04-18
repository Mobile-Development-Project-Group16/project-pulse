package com.bda.projectpulse.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatHistoryDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun getChatCollection(projectId: String) = 
        firestore.collection("projects").document(projectId).collection("chat_history")

    suspend fun saveMessage(projectId: String, message: ChatMessage) {
        getChatCollection(projectId)
            .add(message.toMap())
            .await()
    }

    suspend fun getChatHistory(projectId: String): List<ChatMessage> {
        val snapshot = getChatCollection(projectId)
            .orderBy("timestamp")
            .get()
            .await()
        
        return snapshot.documents.mapNotNull { doc ->
            ChatMessage.fromMap(doc.data ?: return@mapNotNull null)
        }
    }

    suspend fun clearChatHistory(projectId: String) {
        val snapshot = getChatCollection(projectId)
            .get()
            .await()
        
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }
}

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "role" to role,
        "content" to content,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any>): ChatMessage? {
            return try {
                ChatMessage(
                    role = map["role"] as String,
                    content = map["content"] as String,
                    timestamp = (map["timestamp"] as? Long) ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
} 