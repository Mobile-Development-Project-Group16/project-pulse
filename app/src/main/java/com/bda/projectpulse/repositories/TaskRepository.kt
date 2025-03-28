package com.bda.projectpulse.repositories

import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val tasksCollection = firestore.collection("tasks")

    fun getTasks(): Flow<List<Task>> = callbackFlow {
        val subscription = tasksCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { subscription.remove() }
    }

    fun getTaskById(taskId: String): Flow<Task?> = callbackFlow {
        val subscription = tasksCollection
            .document(taskId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val task = snapshot?.toObject(Task::class.java)?.copy(id = snapshot.id)
                trySend(task)
            }

        awaitClose { subscription.remove() }
    }

    fun getProjectTasks(projectId: String): Flow<List<Task>> = callbackFlow {
        val subscription = tasksCollection
            .whereEqualTo("projectId", projectId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { subscription.remove() }
    }

    fun getUserTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val subscription = tasksCollection
            .whereEqualTo("assignedTo", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createTask(task: Task): Result<String> = try {
        val taskWithTimestamp = task.copy(
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        val docRef = tasksCollection.add(taskWithTimestamp).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateTask(taskId: String, task: Task): Result<Unit> = try {
        val taskWithTimestamp = task.copy(
            updatedAt = Timestamp.now()
        )
        tasksCollection.document(taskId).set(taskWithTimestamp).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteTask(taskId: String): Result<Unit> = try {
        tasksCollection.document(taskId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> = try {
        tasksCollection.document(taskId)
            .update("status", status, "updatedAt", Timestamp.now())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun assignTask(taskId: String, userId: String): Result<Unit> = try {
        tasksCollection.document(taskId)
            .update("assignedTo", userId, "updatedAt", Timestamp.now())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addComment(taskId: String, comment: Task.Comment): Result<Unit> = try {
        tasksCollection.document(taskId)
            .update("comments", com.google.firebase.firestore.FieldValue.arrayUnion(comment))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateSubTask(taskId: String, subTask: Task.SubTask): Result<Unit> {
        return try {
            val taskDoc = tasksCollection.document(taskId).get().await()
            val task = taskDoc.toObject(Task::class.java) ?: return Result.failure(Exception("Task not found"))

            val updatedSubTasks = task.subTasks.map { currentSubTask -> 
                if (currentSubTask.id == subTask.id) subTask else currentSubTask 
            }

            tasksCollection.document(taskId)
                .update(mapOf(
                    "subTasks" to updatedSubTasks,
                    "updatedAt" to Timestamp.now()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAttachment(taskId: String, attachment: Task.Attachment): Result<Unit> = try {
        tasksCollection.document(taskId)
            .update("attachments", com.google.firebase.firestore.FieldValue.arrayUnion(attachment))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 