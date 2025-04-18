package com.bda.projectpulse.data

import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskDataSource {
    fun getTaskById(taskId: String): Flow<Task>
    fun getTasksByProject(projectId: String): Flow<List<Task>>
    fun getTasksByAssignee(userId: String): Flow<List<Task>>
    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus)
    suspend fun deleteTask(taskId: String)
} 