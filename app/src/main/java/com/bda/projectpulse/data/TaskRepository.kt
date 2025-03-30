package com.bda.projectpulse.data

import com.bda.projectpulse.models.Task
import com.bda.projectpulse.models.TaskStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDataSource: TaskDataSource
) {
    fun getTaskById(taskId: String): Flow<Task> {
        return taskDataSource.getTaskById(taskId)
    }

    fun getTasksByProject(projectId: String): Flow<List<Task>> {
        return taskDataSource.getTasksByProject(projectId)
    }

    fun getTasksByAssignee(userId: String): Flow<List<Task>> {
        return taskDataSource.getTasksByAssignee(userId)
    }

    suspend fun createTask(task: Task) {
        taskDataSource.createTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDataSource.updateTask(task)
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        taskDataSource.updateTaskStatus(taskId, status)
    }

    suspend fun deleteTask(taskId: String) {
        taskDataSource.deleteTask(taskId)
    }
} 