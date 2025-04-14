package com.bda.projectpulse.models

import com.google.firebase.Timestamp

enum class NotificationType {
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_APPROVED,
    TASK_REJECTED,
    TASK_UPDATED,
    TASK_SUBMITTED,
    CHAT_MESSAGE
}

data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.TASK_ASSIGNED,
    val title: String = "",
    val message: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean = false,
    val data: Map<String, String> = emptyMap() // Additional data like taskId, projectId, etc.
) 