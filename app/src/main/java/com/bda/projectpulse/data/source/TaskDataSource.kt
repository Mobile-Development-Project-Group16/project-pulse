package com.bda.projectpulse.data.source

import com.bda.projectpulse.models.Task

interface TaskDataSource {
    suspend fun createTask(task: Task): Task
} 