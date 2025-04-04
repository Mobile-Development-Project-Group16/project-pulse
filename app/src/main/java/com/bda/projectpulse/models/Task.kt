package com.bda.projectpulse.models

import com.google.firebase.Timestamp

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val projectId: String = "",
    val assigneeIds: List<String> = emptyList(),
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val comments: List<Comment> = emptyList(),
    val subTasks: List<SubTask> = emptyList(),
    val attachments: List<String> = emptyList(),
    val createdBy: String = ""
)

data class Comment(
    val id: String = "",
    val text: String = "",
    val authorId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class SubTask(
    val id: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val assigneeId: String? = null
)

data class Attachment(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val type: String = "",
    val size: Long = 0,
    val uploadedAt: Timestamp = Timestamp.now()
) 