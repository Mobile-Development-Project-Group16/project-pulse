package com.bda.projectpulse.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Task(
    @DocumentId
    val id: String = "",
    
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val projectId: String = "",
    val assignedTo: String? = null,
    val dueDate: Timestamp? = null,
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val tags: List<String> = emptyList(),
    @PropertyName("sub_tasks")
    val subTasks: List<SubTask> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val comments: List<Comment> = emptyList()
) {
    data class Comment(
        val id: String = "",
        val content: String = "",
        val userId: String = "",
        val createdAt: Timestamp = Timestamp.now()
    )

    data class SubTask(
        val id: String = "",
        val title: String = "",
        val isCompleted: Boolean = false,
        val createdAt: Timestamp = Timestamp.now()
    )

    data class Attachment(
        val id: String = "",
        val name: String = "",
        val url: String = "",
        val type: String = "",
        val size: Long = 0,
        val uploadedBy: String = "",
        val uploadedAt: Timestamp = Timestamp.now()
    )
}

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    COMPLETED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
} 