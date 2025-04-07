package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val chatRef = database.reference.child("chats")

    fun getProjectMessages(projectId: String): Flow<List<ChatMessage>> = callbackFlow {
        val messagesRef = chatRef.child(projectId)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    child.getValue(ChatMessage::class.java)?.let { messages.add(it) }
                }
                trySend(messages.sortedBy { it.timestamp.seconds })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    suspend fun sendMessage(message: ChatMessage) {
        val messageRef = chatRef.child(message.projectId).push()
        messageRef.setValue(message.copy(id = messageRef.key ?: "")).await()
    }

    suspend fun deleteMessage(projectId: String, messageId: String) {
        chatRef.child(projectId).child(messageId).removeValue().await()
    }
} 