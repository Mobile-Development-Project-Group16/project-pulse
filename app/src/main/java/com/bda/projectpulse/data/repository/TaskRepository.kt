package com.bda.projectpulse.data.repository

import com.bda.projectpulse.models.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("tasks")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            throw RepositoryException("Failed to fetch tasks", e)
        }
    }

    suspend fun getTasksByProjectId(projectId: String): List<Task> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("tasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            throw RepositoryException("Failed to fetch project tasks", e)
        }
    }

    suspend fun getTaskById(taskId: String): Task = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("tasks")
                .document(taskId)
                .get()
                .await()
            
            doc.toObject(Task::class.java)?.copy(id = doc.id)
                ?: throw RepositoryException("Task not found")
        } catch (e: Exception) {
            throw RepositoryException("Failed to fetch task", e)
        }
    }

    suspend fun createTask(task: Task): String = withContext(Dispatchers.IO) {
        try {
            val docRef = firestore.collection("tasks")
                .add(task)
                .await()
            docRef.id
        } catch (e: Exception) {
            throw RepositoryException("Failed to create task", e)
        }
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        try {
            task.id?.let { taskId ->
                firestore.collection("tasks")
                    .document(taskId)
                    .set(task)
                    .await()
            } ?: throw RepositoryException("Task ID is required for update")
        } catch (e: Exception) {
            throw RepositoryException("Failed to update task", e)
        }
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("tasks")
                .document(taskId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw RepositoryException("Failed to delete task", e)
        }
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause) 