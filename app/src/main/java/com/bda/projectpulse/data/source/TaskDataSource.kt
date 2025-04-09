package com.bda.projectpulse.data.source

import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import com.bda.projectpulse.models.User
import kotlinx.coroutines.flow.Flow

interface TaskDataSource {
    fun getTaskById(taskId: String): Flow<Task>
    fun getTasks(projectId: String): Flow<List<Task>>
    suspend fun createTask(task: Task): Task
    suspend fun updateTask(task: Task)
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus)
    suspend fun deleteTask(taskId: String)
    suspend fun getUsers(): List<User>
} 