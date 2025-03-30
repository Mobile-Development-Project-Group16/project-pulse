package com.bda.projectpulse.data

import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTaskDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : TaskDataSource {

    private val tasksCollection = firestore.collection("tasks")

    override fun getTaskById(taskId: String): Flow<Task> {
        return tasksCollection.document(taskId).snapshots()
            .map { snapshot -> snapshot.toObject<Task>() ?: throw IllegalStateException("Task not found") }
    }

    override fun getTasksByProject(projectId: String): Flow<List<Task>> {
        return tasksCollection
            .whereEqualTo("projectId", projectId)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<Task>() } }
    }

    override fun getTasksByAssignee(userId: String): Flow<List<Task>> {
        return tasksCollection
            .whereEqualTo("assigneeId", userId)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toObject<Task>() } }
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