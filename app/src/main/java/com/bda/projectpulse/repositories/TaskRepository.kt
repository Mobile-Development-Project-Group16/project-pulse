package com.bda.projectpulse.repositories

import android.util.Log
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
import com.bda.projectpulse.models.Notification
import com.bda.projectpulse.models.NotificationType
import com.bda.projectpulse.repositories.NotificationRepository
import com.google.firebase.auth.FirebaseAuth

@Singleton
class TaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val taskDataSource: TaskDataSource,
    private val notificationRepository: NotificationRepository
) {
    private val tasksCollection = firestore.collection("tasks")
    private val usersCollection = firestore.collection("users")
    private val TAG = "TaskRepository"

    suspend fun getTask(taskId: String): Task {
        return taskDataSource.getTaskById(taskId).first()
    }

    suspend fun createTask(task: Task): Task {
        Log.d(TAG, "Creating task: ${task.title}")
        val createdTask = taskDataSource.createTask(task)
        
        // Create notifications for assignees
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && task.assigneeIds.isNotEmpty()) {
            Log.d(TAG, "Creating notifications for ${task.assigneeIds.size} assignees")
            
            // Get creator name for the notification message
            val creator = usersCollection.document(currentUser.uid).get().await()
            val creatorName = creator.getString("displayName") ?: "A team member"
            
            // Send notifications to all assignees
            task.assigneeIds.forEach { assigneeId ->
                if (assigneeId != currentUser.uid) { // Don't notify the creator if they assigned themselves
                    Log.d(TAG, "Creating task assignment notification for user: $assigneeId")
                    val notification = Notification(
                        id = "",
                        type = NotificationType.TASK_ASSIGNED,
                        title = "New Task Assigned",
                        message = "$creatorName assigned you to: ${task.title}",
                        recipientId = assigneeId,
                        senderId = currentUser.uid,
                        timestamp = Timestamp.now(),
                        read = false,
                        data = mapOf(
                            "taskId" to createdTask.id,
                            "projectId" to task.projectId
                        )
                    )
                    notificationRepository.createNotification(notification)
                    Log.d(TAG, "Notification created for task assignment")
                }
            }
        } else {
            Log.d(TAG, "No notifications created: currentUser=${currentUser != null}, assigneeCount=${task.assigneeIds.size}")
        }
        
        return createdTask
    }

    suspend fun updateTask(task: Task) {
        Log.d(TAG, "Updating task: ${task.title} (${task.id}) with status: ${task.status}")
        taskDataSource.updateTask(task)
        
        // Create notification for task updates if there are assignees
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && task.assigneeIds.isNotEmpty()) {
            Log.d(TAG, "Creating notifications for task update, assignees: ${task.assigneeIds.size}")
            
            // Get updater name for the notification message
            val updater = usersCollection.document(currentUser.uid).get().await()
            val updaterName = updater.getString("displayName") ?: "A team member"
            
            // Send notifications to all assignees except the updater
            task.assigneeIds.forEach { assigneeId ->
                if (assigneeId != currentUser.uid) {
                    Log.d(TAG, "Creating task update notification for user: $assigneeId")
                    val notification = Notification(
                        id = "",
                        type = NotificationType.TASK_UPDATED,
                        title = "Task Updated",
                        message = "$updaterName updated task: ${task.title}",
                        recipientId = assigneeId,
                        senderId = currentUser.uid,
                        timestamp = Timestamp.now(),
                        read = false,
                        data = mapOf(
                            "taskId" to task.id,
                            "projectId" to task.projectId
                        )
                    )
                    notificationRepository.createNotification(notification)
                    Log.d(TAG, "Notification created for task update")
                }
            }
        } else {
            Log.d(TAG, "No notifications created for update: currentUser=${currentUser != null}, assigneeCount=${task.assigneeIds.size}")
        }
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return try {
            Log.d(TAG, "Updating task status: $taskId to $status")
            if (taskId.isBlank()) {
                return Result.failure(IllegalArgumentException("Task ID cannot be empty"))
            }
            
            // Get the task to include its details in the notification
            val task = tasksCollection.document(taskId).get().await().toObject(Task::class.java)
                ?: return Result.failure(IllegalArgumentException("Task not found"))
            
            Log.d(TAG, "Found task for status update: ${task.title} (${task.id})")
            
            tasksCollection.document(taskId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
                
            // Create notification for relevant users based on status
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                Log.d(TAG, "Creating notifications for task status change to $status")
                
                // Get updater name for the notification message
                val updater = usersCollection.document(currentUser.uid).get().await()
                val updaterName = updater.getString("displayName") ?: "A team member"
                
                val notificationType = when(status) {
                    TaskStatus.APPROVED -> NotificationType.TASK_APPROVED
                    TaskStatus.IN_REVIEW -> NotificationType.TASK_SUBMITTED
                    TaskStatus.REJECTED -> NotificationType.TASK_REJECTED
                    else -> NotificationType.TASK_UPDATED
                }
                
                // Create notification title based on status
                val notificationTitle = when(status) {
                    TaskStatus.APPROVED -> "Task Approved"
                    TaskStatus.IN_REVIEW -> "Task Submitted for Review"
                    TaskStatus.REJECTED -> "Task Rejected"
                    TaskStatus.TODO -> "Task Moved to To-Do"
                    TaskStatus.IN_PROGRESS -> "Task In Progress"
                    else -> "Task Status Updated"
                }
                
                // Create notification message based on status
                val notificationMessage = when(status) {
                    TaskStatus.APPROVED -> "$updaterName approved task '${task.title}'"
                    TaskStatus.IN_REVIEW -> "$updaterName submitted task '${task.title}' for review"
                    TaskStatus.REJECTED -> "$updaterName rejected task '${task.title}'"
                    TaskStatus.TODO -> "$updaterName moved task '${task.title}' to To-Do"
                    TaskStatus.IN_PROGRESS -> "$updaterName started working on task '${task.title}'"
                    else -> "$updaterName updated the status of task '${task.title}' to ${status.name}"
                }
                
                // Determine recipients for notifications
                val notificationRecipients = mutableSetOf<String>()
                
                // Always notify the task creator if they're not the one updating
                if (task.createdBy != currentUser.uid) {
                    notificationRecipients.add(task.createdBy)
                    Log.d(TAG, "Adding task creator ${task.createdBy} to notification recipients")
                }
                
                // Special handling for IN_REVIEW status (task submissions)
                if (status == TaskStatus.IN_REVIEW) {
                    try {
                        Log.d(TAG, "Processing IN_REVIEW status for task ${task.id}, getting project details")
                        // Get the project to find the project owner and managers
                        val projectDoc = firestore.collection("projects").document(task.projectId).get().await()
                        if (projectDoc.exists()) {
                            val projectData = projectDoc.data
                            if (projectData != null) {
                                Log.d(TAG, "Found project data for project ID: ${task.projectId}")
                                
                                // Add project owner to recipients if they're not already included
                                val projectOwnerId = projectData["ownerId"] as? String
                                if (projectOwnerId != null && projectOwnerId != currentUser.uid && !notificationRecipients.contains(projectOwnerId)) {
                                    notificationRecipients.add(projectOwnerId)
                                    Log.d(TAG, "Adding project owner $projectOwnerId to notification recipients for review")
                                } else {
                                    Log.d(TAG, "Project owner is either null, the current user, or already included")
                                }
                                
                                // For submitted tasks, also notify any users with MANAGER role in the project
                                val projectManagerIds = projectData["managerIds"] as? List<String> ?: emptyList()
                                if (projectManagerIds.isNotEmpty()) {
                                    Log.d(TAG, "Found ${projectManagerIds.size} managers for the project")
                                    projectManagerIds.forEach { managerId ->
                                        if (managerId != currentUser.uid && !notificationRecipients.contains(managerId)) {
                                            notificationRecipients.add(managerId)
                                            Log.d(TAG, "Adding project manager $managerId to notification recipients for review")
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "No managers found for the project")
                                }
                            } else {
                                Log.e(TAG, "Project document exists but data is null for project ID: ${task.projectId}")
                            }
                        } else {
                            Log.e(TAG, "Project document does not exist for project ID: ${task.projectId}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting project details for notifications", e)
                    }
                }
                
                // For all status types, notify other assignees
                task.assigneeIds.forEach { assigneeId ->
                    if (assigneeId != currentUser.uid) {
                        notificationRecipients.add(assigneeId)
                        Log.d(TAG, "Adding assignee $assigneeId to notification recipients")
                    }
                }
                
                Log.d(TAG, "Will send status change notifications to ${notificationRecipients.size} recipients: $notificationRecipients")
                
                if (notificationRecipients.isEmpty()) {
                    Log.w(TAG, "No recipients found for notification. This could be an issue.")
                }
                
                // Send notifications to all recipients with retry logic
                notificationRecipients.forEach { recipientId ->
                    Log.d(TAG, "Creating task status notification for user: $recipientId, status: $status")
                    val notification = Notification(
                        id = "",
                        type = notificationType,
                        title = notificationTitle,
                        message = notificationMessage,
                        recipientId = recipientId,
                        senderId = currentUser.uid,
                        timestamp = Timestamp.now(),
                        read = false,
                        data = mapOf(
                            "taskId" to taskId,
                            "projectId" to task.projectId
                        )
                    )
                    
                    try {
                        notificationRepository.createNotification(notification)
                        Log.d(TAG, "Notification successfully created for recipient $recipientId")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating notification for recipient $recipientId, will retry", e)
                        // Retry once after a short delay
                        try {
                            // Wait a moment before retrying
                            kotlinx.coroutines.delay(1000)
                            notificationRepository.createNotification(notification)
                            Log.d(TAG, "Notification successfully created on retry for recipient $recipientId")
                        } catch (e2: Exception) {
                            Log.e(TAG, "Error creating notification for recipient $recipientId after retry", e2)
                        }
                    }
                }
            } else {
                Log.e(TAG, "No notifications created for status update: currentUser is null")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task status", e)
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
                
                // Create notification for the assigned user
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val notification = Notification(
                        id = "",
                        type = NotificationType.TASK_ASSIGNED,
                        title = "New Task Assigned",
                        message = "You have been assigned to task: ${task.title}",
                        recipientId = userId,
                        senderId = currentUser.uid,
                        timestamp = Timestamp.now(),
                        read = false,
                        data = mapOf(
                            "taskId" to taskId,
                            "projectId" to task.projectId
                        )
                    )
                    notificationRepository.createNotification(notification)
                }
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