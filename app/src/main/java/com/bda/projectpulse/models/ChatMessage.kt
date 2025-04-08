package com.bda.projectpulse.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class ChatMessage(
    @DocumentId
    val id: String = "",
    val projectId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    @PropertyName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    val isFromCurrentUser: Boolean = false
)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE
} 