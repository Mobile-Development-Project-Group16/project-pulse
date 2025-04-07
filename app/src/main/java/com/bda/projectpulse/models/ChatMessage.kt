package com.bda.projectpulse.models

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val projectId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE
} 