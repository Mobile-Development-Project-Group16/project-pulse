package com.bda.projectpulse.data

import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTaskDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : TaskDataSource {

    private val tasksCollection = firestore.collection("tasks")

    override fun getTaskById(taskId: String): Flow<Task> = callbackFlow {
        val subscription = tasksCollection.document(taskId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val task = snapshot.toObject(Task::class.java)
                if (task != null) {
                    trySend(task)
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getTasksByProject(projectId: String): Flow<List<Task>> = callbackFlow {
        val subscription = tasksCollection
            .whereEqualTo("projectId", projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
                    trySend(tasks)
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getTasksByAssignee(userId: String): Flow<List<Task>> = callbackFlow {
        val subscription = tasksCollection
            .whereEqualTo("assigneeId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
                    trySend(tasks)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun createTask(task: Task) {
        val taskData = task.copy(id = tasksCollection.document().id)
        tasksCollection.document(taskData.id).set(taskData).await()
    }

    override suspend fun updateTask(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        tasksCollection.document(taskId).update("status", status).await()
    }

    override suspend fun deleteTask(taskId: String) {
        tasksCollection.document(taskId).delete().await()
    }
} 