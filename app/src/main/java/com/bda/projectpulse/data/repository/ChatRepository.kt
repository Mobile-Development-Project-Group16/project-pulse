package com.bda.projectpulse.data.repository

import com.bda.projectpulse.models.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getMessages(projectId: String): List<ChatMessage> {
        return firestore.collection("projects")
            .document(projectId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(ChatMessage::class.java)
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
} 