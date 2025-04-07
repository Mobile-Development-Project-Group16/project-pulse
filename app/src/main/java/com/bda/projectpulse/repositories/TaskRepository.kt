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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val taskDataSource: TaskDataSource
) {
    private val tasksCollection = firestore.collection("tasks")

    suspend fun createTask(task: Task, userId: String): Task {
        try {
            println("TaskRepository: Starting task creation for title: ${task.title}")
            
            // Validate task data
            if (task.title.isBlank()) {
                throw IllegalArgumentException("Task title cannot be empty")
            }
            if (task.projectId.isBlank()) {
                throw IllegalArgumentException("Project ID cannot be empty")
            }
            
            // Create task with creator ID
            val taskWithCreator = task.copy(
                createdBy = userId
            )
            
            println("TaskRepository: Calling taskDataSource.createTask")
            val createdTask = taskDataSource.createTask(taskWithCreator)
            println("TaskRepository: Task created successfully with ID: ${createdTask.id}")
            
            return createdTask
        } catch (e: Exception) {
            println("TaskRepository: Error creating task - ${e.message}")
            println("TaskRepository: Stack trace - ${e.stackTrace.joinToString("\n")}")
            throw e
        }
    }

    suspend fun updateTask(task: Task) {
        try {
            val taskData = hashMapOf(
                "title" to task.title,
                "description" to task.description,
                "projectId" to task.projectId,
                "assigneeIds" to task.assigneeIds,
                "status" to task.status.name,
                "priority" to task.priority.name,
                "dueDate" to task.dueDate,
                "updatedAt" to Timestamp.now(),
                "comments" to task.comments,
                "subTasks" to task.subTasks,
                "attachments" to task.attachments
            )
            tasksCollection.document(task.id).update(taskData as Map<String, Any>).await()
        } catch (e: Exception) {
            throw e
        }
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

    fun getTasksByProject(projectId: String): Flow<List<Task>> = flow {
        try {
            val snapshot = tasksCollection.whereEqualTo("projectId", projectId).get().await()
            val tasks = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    Task(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        projectId = data["projectId"] as? String ?: "",
                        assigneeIds = (data["assigneeIds"] as? List<String>) ?: emptyList(),
                        status = TaskStatus.valueOf(data["status"] as? String ?: TaskStatus.TODO.name),
                        priority = TaskPriority.valueOf(data["priority"] as? String ?: TaskPriority.MEDIUM.name),
                        dueDate = data["dueDate"] as? Timestamp,
                        createdAt = (data["createdAt"] as? Timestamp) ?: Timestamp.now(),
                        updatedAt = (data["updatedAt"] as? Timestamp) ?: Timestamp.now(),
                        comments = (data["comments"] as? List<Map<String, Any>>)?.map { commentData ->
                            Comment(
                                id = commentData["id"] as? String ?: "",
                                text = commentData["text"] as? String ?: "",
                                authorId = commentData["authorId"] as? String ?: "",
                                createdAt = (commentData["createdAt"] as? Timestamp) ?: Timestamp.now(),
                                updatedAt = (commentData["updatedAt"] as? Timestamp) ?: Timestamp.now()
                            )
                        } ?: emptyList(),
                        subTasks = (data["subTasks"] as? List<Map<String, Any>>)?.map { subTaskData ->
                            SubTask(
                                id = subTaskData["id"] as? String ?: "",
                                title = subTaskData["title"] as? String ?: "",
                                completed = subTaskData["completed"] as? Boolean ?: false,
                                assigneeId = subTaskData["assigneeId"] as? String
                            )
                        } ?: emptyList(),
                        attachments = (data["attachments"] as? List<String>) ?: emptyList(),
                        createdBy = (data["createdBy"] as? String) ?: ""
                    )
                }
            }
            emit(tasks)
        } catch (e: Exception) {
            throw e
        }
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
} 