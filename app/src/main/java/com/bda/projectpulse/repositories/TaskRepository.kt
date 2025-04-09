package com.bda.projectpulse.repositories

import com.bda.projectpulse.data.source.TaskDataSource
import com.bda.projectpulse.models.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val taskDataSource: TaskDataSource
) {
    private val tasksCollection = firestore.collection("tasks")
    private val usersCollection = firestore.collection("users")

    suspend fun getTask(taskId: String): Task {
        return taskDataSource.getTaskById(taskId).first()
    }

    suspend fun createTask(task: Task): Task {
        return taskDataSource.createTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDataSource.updateTask(task)
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return try {
            if (taskId.isBlank()) {
                return Result.failure(IllegalArgumentException("Task ID cannot be empty"))
            }
            
            tasksCollection.document(taskId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            tasksCollection.document(taskId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTaskById(taskId: String): Flow<Task?> = flow {
        try {
            val snapshot = tasksCollection.document(taskId).get().await()
            val task = snapshot.toObject(Task::class.java)
            emit(task?.copy(id = snapshot.id))
        } catch (e: Exception) {
            throw e
        }
    }

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

    fun getProjectTasks(projectId: String, currentUserRole: UserRole, currentUserId: String): Flow<List<Task>> = callbackFlow {
        val subscription = tasksCollection
            .whereEqualTo("projectId", projectId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val allTasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                // Filter tasks based on user role
                val filteredTasks = when (currentUserRole) {
                    UserRole.ADMIN -> allTasks // Admins can see all tasks
                    UserRole.MANAGER -> allTasks.filter { task ->
                        // Managers can see tasks they created or are assigned to
                        task.createdBy == currentUserId || task.assigneeIds.contains(currentUserId)
                    }
                    else -> allTasks.filter { task ->
                        // Regular users can only see tasks assigned to them
                        task.assigneeIds.contains(currentUserId)
                    }
                }

                trySend(filteredTasks)
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

    suspend fun assignTask(taskId: String, userId: String): Result<Unit> {
        return try {
            val task = tasksCollection.document(taskId).get().await().toObject(Task::class.java)
            if (task != null) {
                val updatedAssignees = task.assigneeIds + userId
                tasksCollection.document(taskId)
                    .update("assigneeIds", updatedAssignees, "updatedAt", Timestamp.now())
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(taskId: String, comment: Comment): Result<Unit> {
        return try {
            tasksCollection.document(taskId)
                .update("comments", com.google.firebase.firestore.FieldValue.arrayUnion(comment))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSubTask(taskId: String, subTask: SubTask): Result<Unit> {
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

    suspend fun addAttachment(taskId: String, attachmentUrl: String): Result<Unit> {
        return try {
            tasksCollection.document(taskId)
                .update("attachments", com.google.firebase.firestore.FieldValue.arrayUnion(attachmentUrl))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTaskPriority(taskId: String, priority: TaskPriority): Result<Unit> {
        return try {
            tasksCollection.document(taskId)
                .update("priority", priority.name, "updatedAt", Timestamp.now())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTasks(projectId: String): Flow<List<Task>> {
        return taskDataSource.getTasks(projectId)
    }

    suspend fun getUsers(): List<User> {
        return taskDataSource.getUsers()
    }
} 