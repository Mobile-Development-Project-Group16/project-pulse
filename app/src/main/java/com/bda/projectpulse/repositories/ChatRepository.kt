package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getProjectMessages(projectId: String): Flow<List<ChatMessage>> = flow {
        val messages = firestore.collection("projects")
            .document(projectId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(ChatMessage::class.java)
        emit(messages)
    }

    suspend fun sendMessage(projectId: String, text: String) {
        val message = ChatMessage(
            projectId = projectId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        firestore.collection("projects")
            .document(projectId)
            .collection("messages")
            .add(message)
            .await()
    }

    suspend fun deleteMessage(projectId: String, messageId: String) {
        firestore.collection("projects")
            .document(projectId)
            .collection("messages")
            .document(messageId)
            .delete()
            .await()
    }
} 