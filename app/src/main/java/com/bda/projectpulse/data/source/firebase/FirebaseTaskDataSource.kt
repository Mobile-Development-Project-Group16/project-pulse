package com.bda.projectpulse.data.source.firebase

import com.bda.projectpulse.data.source.TaskDataSource
import com.bda.projectpulse.models.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseTaskDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : TaskDataSource {
    private val tasksCollection = firestore.collection("tasks")

    override suspend fun createTask(task: Task): Task = withContext(Dispatchers.IO) {
        try {
            println("FirebaseTaskDataSource: Starting task creation in Firestore")
            
            val taskData = hashMapOf(
                "title" to task.title,
                "description" to task.description,
                "projectId" to task.projectId,
                "assigneeIds" to task.assigneeIds,
                "status" to task.status.name,
                "priority" to task.priority.name,
                "dueDate" to task.dueDate,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "comments" to task.comments,
                "subTasks" to task.subTasks,
                "attachments" to task.attachments,
                "createdBy" to task.createdBy
            )
            
            println("FirebaseTaskDataSource: Adding task to Firestore")
            val docRef = tasksCollection.add(taskData).await()
            println("FirebaseTaskDataSource: Task added with ID: ${docRef.id}")
            
            return@withContext task.copy(id = docRef.id)
        } catch (e: Exception) {
            println("FirebaseTaskDataSource: Error creating task - ${e.message}")
            println("FirebaseTaskDataSource: Stack trace - ${e.stackTrace.joinToString("\n")}")
            throw e
        }
    }
} 