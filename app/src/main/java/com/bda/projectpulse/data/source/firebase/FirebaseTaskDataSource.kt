package com.bda.projectpulse.data.source.firebase

import com.bda.projectpulse.data.source.TaskDataSource
import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseTaskDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : TaskDataSource {
    private val tasksCollection = firestore.collection("tasks")
    private val usersCollection = firestore.collection("users")

    override fun getTaskById(taskId: String): Flow<Task> = flow {
        val snapshot = tasksCollection.document(taskId).get().await()
        val task = snapshot.toObject(Task::class.java)
        emit(task?.copy(id = snapshot.id) ?: throw Exception("Task not found"))
    }

    override fun getTasks(projectId: String): Flow<List<Task>> = flow {
        val snapshot = tasksCollection.whereEqualTo("projectId", projectId).get().await()
        val tasks = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Task::class.java)?.copy(id = doc.id)
        }
        emit(tasks)
    }

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

    override suspend fun updateTask(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        tasksCollection.document(taskId).update(
            mapOf(
                "status" to status.name,
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }

    override suspend fun deleteTask(taskId: String) {
        tasksCollection.document(taskId).delete().await()
    }

    override suspend fun getUsers(): List<User> {
        val snapshot = usersCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(uid = doc.id)
        }
    }
} 